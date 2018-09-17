package com.pelatro.adaptor.ncell.common;

import static com.pelatro.adaptor.ncell.common.Labels.CTX_DUMMY;
import static com.pelatro.adaptor.ncell.common.Labels.CTX_EVENT;
import static com.pelatro.adaptor.ncell.common.Labels.CTX_FILE_COUNTER;
import static com.pelatro.adaptor.ncell.common.Labels.CTX_FILTER;
import static com.pelatro.adaptor.ncell.common.Labels.CTX_LK_MSISDN;
import static com.pelatro.adaptor.ncell.common.Labels.CTX_LV_SUBID;
import static com.pelatro.adaptor.ncell.common.Labels.CTX_LV_UID;
import static com.pelatro.adaptor.ncell.common.Labels.CTX_RAW;
import static com.pelatro.adaptor.ncell.common.Labels.CTX_REASON;
import static com.pelatro.adaptor.ncell.common.Labels.CTX_RECORD;
import static com.pelatro.adaptor.ncell.common.Labels.CTX_SUMMARY;

import java.io.Closeable;
import java.io.IOException;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;

import com.pelatro.adaptor.common.Adaptor;
import com.pelatro.adaptor.ncell.client.FileTracker;
import com.pelatro.adaptor.ncell.client.ServiceCallback;
import com.pelatro.adaptor.ncell.client.SubscriberServiceLookup;
import com.pelatro.adaptor.ncell.client.ToolsFactory;
import com.pelatro.adaptor.ncell.db.UniqueIDGenerator;
import com.pelatro.adaptor.streams.Annals;
import com.pelatro.adaptor.streams.Context;
import com.pelatro.adaptor.streams.EventType;
import com.pelatro.adaptor.streams.Publisher;
import com.pelatro.adaptor.streams.Stream;

public abstract class NcellAdaptor implements Adaptor{

	private static final Logger logger = LogManager.getLogger( NcellAdaptor.class );

	private ParseSummaryWithDBLogging currentSummary;
	protected final AdaptorType adaptorType;
	protected final TransactionManager txManager;
	protected Stream stream;

	public NcellAdaptor( AdaptorType adaptorType ) {
		this.txManager = new TransactionManager( 50 );
		this.adaptorType = adaptorType;
	}

	@Override
	public void process( Stream stream ) {
		if ( this.stream != null && this.stream != stream )
			throw new UnsupportedOperationException( "Cannot change boats in mid-stream" );

		this.stream = stream;
		txManager.open();
		UniqueIDGenerator.open();
			logger.info( String.format( "Run started" ) );
			while ( stream.execute( EventType.ON_NEXT ) ) {
				try {
					processFile( stream );
				}
				catch ( HibernateException e ) {
					fail( "DB related exception. Executing ON_FAIL and terminating", e );
					stream.execute( EventType.ON_FAIL );
					throw e;
				}
				catch ( Throwable e ) {
					fail( "Error while parsing file. Executing ON_FAIL", e );
					String reason =
							String.format( "Error while parsing file. %s", e.getMessage() );
					stream.context.set( CTX_REASON, reason );
					stream.execute( EventType.ON_FAIL );
				}
				finally {
				try {
					Thread.sleep( 5000 );
				}
				catch ( InterruptedException e ) {
					logger.error( e );
				}
			}
	}
		
			logger.info( String.format( "Run ended" ) );
			UniqueIDGenerator.close();
			txManager.close();

		}
	private void fail( String message, Throwable e ) {
		logger.error( message, e );

		String reason = String.format( "%s: %s", message, e.getMessage() );
		stream.context.set( CTX_REASON, reason );
		stream.execute( EventType.ON_FAIL );
	}


	protected abstract void processFile( Stream stream ) throws Exception;

	}
