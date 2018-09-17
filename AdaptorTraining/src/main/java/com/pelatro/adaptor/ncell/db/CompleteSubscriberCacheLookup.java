package com.pelatro.adaptor.ncell.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.StatelessSession;
import org.hibernate.criterion.Order;
import org.joda.time.DateTime;

import com.pelatro.adaptor.ncell.common.Constants;
import com.pelatro.adaptor.ncell.db.dto.CompleteSubscriber;
import com.pelatro.adaptor.ncell.db.dto.Subscriber;

public class CompleteSubscriberCacheLookup {

	private static Logger logger = LogManager.getLogger( CompleteSubscriberCacheLookup.class );
	private static HashMap<String, List<Subscriber>> data =
			new HashMap<String, List<Subscriber>>();
	public static final long start = getUsedMemory();
	static DateTime referenceDate = new DateTime( 2000, 01, 01, 00, 00, 00 );

	public static void main( String[] args ) {
		System.out.println( start / ( 1024 * 1024 ) + " MB" );
		_prepareCache( data );

	}

	public CompleteSubscriberCacheLookup() {
		logger.info( "Building Cache For Subscriber Lookup" );
		_prepareCache( data );
	}

	private static long getUsedMemory() {
		System.gc();
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	}

	public Subscriber query( String subscriberNumber ) {
		String key = subscriberNumber.substring( 3 );
		List<Subscriber> subscribers = data.get( key );
		if ( subscribers == null )
			return null;
		if ( subscribers.size() <= 0 )
			return null;

		return subscribers.get( 0 );
	}

	public void close() {
		data = null;
	}

	public static long getDaysBetween( DateTime earlier, DateTime later ) {
		return ( long ) TimeUnit.MILLISECONDS
				.toMillis( later.getMillis() - earlier.getMillis() );
	}

	private static void _prepareCache( HashMap<String, List<Subscriber>> data ) {
		StatelessSession session =
				DbHenchman.getHenchman().getSessionFactory().openStatelessSession();

		Criteria criteria = session.createCriteria( CompleteSubscriber.class );
		criteria
				.addOrder( Order.asc( "msisdn" ) )
				.addOrder( Order.desc( "id" ) )
				.setCacheable( false )
				.setReadOnly( true )
				.setFetchSize( 100000 );

		int i = 0;
		ScrollableResults cursor = criteria.scroll( ScrollMode.FORWARD_ONLY );
		while ( cursor.next() ) {
			CompleteSubscriber row = ( CompleteSubscriber ) cursor.get( 0 );

			if ( row.getMsisdn() == null
					|| row.getMsisdn().length() < 2
					|| row.getMsisdn().length() > 14 ) {

				logger.info( String.format(
						"Will not cache %s. MSISDN must be between 2 and 14 chars long",
						row.getMsisdn() ) );
				continue;
			}

			String key = row.getMsisdn().substring( 3 );
			List<Subscriber> subscribers = data.get( key );
			if ( subscribers == null ) {
				subscribers = new ArrayList<>( 1 );
				data.put( key, subscribers );
			}
			else {
				logger.info( "Repeating subscriber " + row.getMsisdn() );
			}

			int insertIndex = 0;
			Subscriber sub = new Subscriber();

			for ( ; insertIndex < subscribers.size(); insertIndex++ ) {
				if ( getDaysBetween( Constants.referenceDate,
						new DateTime( row.getDateOfJoining() ) ) > subscribers
								.get( insertIndex ).getDoj() )
					break;
			}
			sub.setId( row.getId() );
			sub.setMsisdn( row.getMsisdn() );
			sub.setStatus( ( byte ) row.getStatus() );
			sub.setMainProduct( Integer
					.parseInt( row.getMainProduct() == null || row.getMainProduct().isEmpty() ? "0" : row.getMainProduct() ) );
			sub.setDoj( row.getDateOfJoining() == null ? 0
					: getDaysBetween( Constants.referenceDate,
							new DateTime( row.getDateOfJoining().getTime() ) ) );
			sub.setType( ( byte ) row.getType() );
			sub.setSubscriberkey( row.getSubscriberkey() );
			sub.setAccountkey( row.getAccount_Key() );
			subscribers.add( insertIndex, sub );
			if ( ( ++i ) % 100000 == 0 ) {
				logger.info( String.format( "... %d rows cached", i ) );
				System.out.println( String.format( "... %d rows cached", i ) );
			}
		}

		logger.info( String.format( "... %d rows cached. Cache built.", i ) );
		session.close();
	}

	public void add( String subscriberNumber, Subscriber subscriber ) {
		String key = subscriberNumber.substring( 3 );
		List<Subscriber> subscribers = data.get( key );
		if ( subscribers == null ) {
			subscribers = new ArrayList<>( 1 );
			data.put( key, subscribers );
		}
		else {
			logger.info( "Repeating subscriber " + subscriber.getMsisdn() );
		}

		int insertIndex = 0;

		for ( ; insertIndex < subscribers.size(); insertIndex++ ) {
			if ( subscriber.getDoj() > subscribers.get( insertIndex )
					.getDoj() )
				break;
		}
		subscribers.add( insertIndex, subscriber );
	}
}
