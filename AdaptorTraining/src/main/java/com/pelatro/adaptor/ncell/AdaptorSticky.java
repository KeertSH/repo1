package com.pelatro.adaptor.ncell;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.pelatro.adaptor.streams.Context;
import com.pelatro.adaptor.streams.Sticky;

public class AdaptorSticky implements Sticky {

	public static final String STICKY_DATE = "%date%";

	private static final DateTimeFormatter DATE_FORMATTER =
			DateTimeFormat.forPattern( "CCYYMMdd" );

	@Override
	public void paste( Context context ) {
		pasteDate( context );
	}

	private void pasteDate( Context context ) {
		context.set( STICKY_DATE, DATE_FORMATTER.print( DateTime.now() ) );
	}
}
