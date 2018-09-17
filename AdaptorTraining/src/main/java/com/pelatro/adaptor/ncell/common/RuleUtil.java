package com.pelatro.adaptor.ncell.common;

import java.util.Map;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

public class RuleUtil<S extends Enum< ? >> {

	private IllegalArgumentException parseException;
	private String failureReason;

	public RuleUtil( IllegalArgumentException parseException ) {
		this.parseException = parseException;
	}

	public void clear() {
		failureReason = null;
	}

	public String getFailureReason() {
		return failureReason;
	}

	private boolean isEmpty( String raw ) {
		return ( raw == null || raw.isEmpty() || raw.equalsIgnoreCase( "NULL" ) );
	}

	public boolean isNullEmpty( String raw ) {
		if ( raw == null || raw.trim().isEmpty() || raw.equalsIgnoreCase( "null" ) )
			return true;
		return false;
	}

	public String mandate( String raw, S s ) {
		if ( isEmpty( raw ) ) {
			failureReason = String.format( "Required field %s is missing", s.name() );
			throw parseException;
		}

		return raw;
	}

	public String matchNonStrictIn(
		Map<String, String> map, String raw, S s, String defaultValue ) {

		if ( isEmpty( raw ) )
			return defaultValue;

		String value = map.get( raw );
		if ( value == null )
			return defaultValue;

		return value;
	}

	public DateTime extractDate(
		DateTimeFormatter formatter, String pattern, String raw, S s ) {
		mandate( raw, s );
		try {
			DateTime startTime = formatter.parseDateTime( raw );
			return startTime;
		}
		catch ( IllegalArgumentException e ) {
			failureReason = String.format( "%s does not conform to '%s' : %s",
					s.name(), pattern, raw );
			throw parseException;
		}
	}

	public String transformAmount( String raw, S s ) {
		Double amount = extractAmount( raw, s );
		return Constants.DECIMAL_FORMAT.format( amount );
	}

	public Double extractAmount( String raw, S s ) {
		mandate( raw, s );
		try {
			return Double.parseDouble( raw ) / Constants.CURRENCY_DIVISOR;
		}
		catch ( NumberFormatException e ) {
			failureReason = String.format( "%s is not a float: %s", s.name(), raw );
			throw parseException;
		}
	}

	public Double extractMB( String raw, S s ) {
		mandate( raw, s );
		try {
			return Double.parseDouble( raw ) / Constants.MB_CURRENCY_DIVISOR;
		}
		catch ( NumberFormatException e ) {
			failureReason = String.format( "%s is not a float: %s", s.name(), raw );
			throw parseException;
		}
	}

	public String matchStrictPattern( Pattern pattern, String raw, S s ) {
		raw = mandate( raw, s );
		raw = matchPattern( pattern, raw, s );
		return raw;
	}

	private String matchPattern( Pattern pattern, String raw, S s ) {

		if ( !pattern.matcher( raw ).matches() ) {
			failureReason = String.format( "%s should conform to pattern %s : %s",
					s.name(), pattern.pattern(), raw );
			throw parseException;
		}

		return raw;
	}

	public String matchNonStrictPattern( Pattern pattern, String raw, S s ) {
		if ( !isEmpty( raw ) )
			matchPattern( pattern, raw, s );

		return raw;
	}

	public String emptyCheck( String raw ) {
		return raw.equals( null ) || raw.isEmpty() || raw.equalsIgnoreCase( "NULL" )
				? Constants.EMPTY : raw;
	}

	public String addCountryCode( String raw ) {
		if ( '+' == raw.charAt( 0 ) || '0' == raw.charAt( 0 ) )
			raw = raw.substring( 1 );

		if ( raw.length() <= 10 && !( raw.startsWith( Constants.NCELL_COUNTRY_CODE ) ) )
			raw = Constants.NCELL_COUNTRY_CODE + raw;

		return raw;
	}

	public String NACheck( String raw ) {
		return raw.equals( null ) || raw.isEmpty() || raw.equalsIgnoreCase( "NULL" )
				? Constants.NA : raw;
	}

	public String TFCheck( String raw ) {
		return raw.equals( null ) || raw.isEmpty() || raw.equalsIgnoreCase( "NULL" )
				? "0" : raw.toUpperCase().equals( "Y" ) ? "1" : "0";
	}

	public String transformDate( DateTimeFormatter formatter, String pattern, String raw,
		S s ) {
		DateTime dttm = extractDate( formatter, pattern, raw, s );
		return Constants.PROCESSED_RECORD_DATE_FORMATTER.print( dttm );
	}

	public String transformbalance( String raw, S s ) {
		if ( !raw.equals( "0" ) ) {
			Double amount = extractAmount( raw, s );
			return Constants.DECIMAL_FORMAT.format( amount );
		}
		else
			return "0.0000";
	}

	public String transformMB( String raw, S s ) {
		if ( !raw.equals( "0" ) ) {
			Double amount = extractMB( raw, s );
			return Constants.DECIMAL_FORMAT.format( amount );
		}
		else
			return "0.0000";
	}

}
