package com.pelatro.adaptor.ncell.db;

import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.StatelessSession;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.pelatro.adaptor.ncell.common.Constants;
import com.pelatro.adaptor.ncell.db.dto.Period;

public class PeriodLookup {

	private static final HashMap<String, String> periodRange = new HashMap<String, String>();
	private static final Logger logger = LogManager.getLogger( PeriodLookup.class );
	private static final DateTimeFormatter timeFormatter =
			DateTimeFormat.forPattern( "HH24:mm:ss" );

	@SuppressWarnings( "unchecked" )
	public static void initialize() {

		DbHenchman henchman = DbHenchman.getHenchman();
		StatelessSession session =
				henchman.getSession();

		logger.info( "Loading periods..." );

		Query query = session.createQuery( " from Period " );

		List<Period> periodList = query.list();

		logger.info( String.format( "... %d Periods slurped ...", periodList.size() ) );

		for ( Period period : periodList )
			periodRange.put( period.getPeriodType(), period.getPeriod() );

		logger.info( String.format( "... %d Periods registered..", periodList.size() ) );

		henchman.close();
	}

	public static String lookup( DateTime dttm ) {

		String dttmInString = timeFormatter.print( dttm );
		dttm = timeFormatter.parseDateTime( dttmInString );
		String[] nightrange = null;
		String[] dayrange = null;
		String[] peakrange = null;
		if ( periodRange.isEmpty() ) {
			return Constants.Period.UNKNOWN;
		}

		if ( periodRange.get( Constants.Period.NIGHT ) != null
				&& !periodRange.get( Constants.Period.NIGHT ).isEmpty() ) {
			nightrange = periodRange.get( Constants.Period.NIGHT ).split( "," );
		}

		if ( periodRange.get( Constants.Period.DAY ) != null
				&& !periodRange.get( Constants.Period.DAY ).isEmpty() ) {
			dayrange = periodRange.get( Constants.Period.DAY ).split( "," );
		}

		if ( periodRange.get( Constants.Period.PEAK ) != null
				&& !periodRange.get( Constants.Period.PEAK ).isEmpty() ) {
			peakrange = periodRange.get( Constants.Period.PEAK ).split( "," );
		}

		for ( int i = 0; nightrange != null && i < nightrange.length; i++ ) {
			int startHour = Integer.parseInt( nightrange[i].split( "-" )[0] );
			int endHour = Integer.parseInt( nightrange[i].split( "-" )[1] );
			Range r = new Range( startHour, endHour );
			if ( r.contains( dttm.getHourOfDay() ) )
				return Constants.Period.NIGHT;
		}

		for ( int i = 0; dayrange != null && i < dayrange.length; i++ ) {
			int startHour = Integer.parseInt( dayrange[i].split( "-" )[0] );
			int endHour = Integer.parseInt( dayrange[i].split( "-" )[1] );
			Range r = new Range( startHour, endHour );

			if ( r.contains( dttm.getHourOfDay() ) )
				return Constants.Period.DAY;
		}

		for ( int i = 0; peakrange != null && i < peakrange.length; i++ ) {
			int startHour = Integer.parseInt( peakrange[i].split( "-" )[0] );
			int endHour = Integer.parseInt( peakrange[i].split( "-" )[1] );
			Range r = new Range( startHour, endHour );
			if ( r.contains( dttm.getHourOfDay() ) )
				return Constants.Period.PEAK;
		}

		return Constants.Period.UNKNOWN;
	}
}
