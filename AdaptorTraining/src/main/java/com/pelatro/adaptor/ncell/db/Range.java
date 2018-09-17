package com.pelatro.adaptor.ncell.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Range {

	private int low;
	private int high;

	public Range( int low, int high ) {
		this.low = low;
		this.high = high;
	}

	public boolean contains( int number ) {
		if ( low > high ) {
			if ( number == low ) {
				return true;
			}
			else {
				try {
					Date time1 =
							new SimpleDateFormat( "HH" ).parse( String.valueOf( low ) );
					Calendar calendar1 = Calendar.getInstance();
					calendar1.setTime( time1 );

					Date time2 = new SimpleDateFormat( "HH" ).parse( String.valueOf( high ) );
					Calendar calendar2 = Calendar.getInstance();
					calendar2.setTime( time2 );
					calendar2.add( Calendar.DATE, 1 );

					Date d = new SimpleDateFormat( "HH" )
							.parse( String.valueOf( number ) );
					Calendar calendar8 = Calendar.getInstance();
					calendar8.setTime( d );
					calendar8.add( Calendar.DATE, 1 );

					Date x = calendar8.getTime();
					return x.after( calendar1.getTime() ) && x.before( calendar2.getTime() );

				}
				catch ( ParseException e ) {
					e.printStackTrace();
					return false;
				}
			}
		}

		return ( ( number >= low && number < high )
				|| ( number > low && number < high ) );

	}

	public boolean numcontains( long number ) {
		return ( ( number <= low && number < high ) || ( number >= low && number > high ) );
	}
}
