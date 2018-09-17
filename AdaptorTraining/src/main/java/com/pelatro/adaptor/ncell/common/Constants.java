package com.pelatro.adaptor.ncell.common;

import java.text.DecimalFormat;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Constants {

	public static final class RowType {

		public static final String OCS_VOICE = "1";

		public static final String OCS_SMS = "2";

		public static final String OCS_DATA = "3";

		public static final String RECHARGE = "4";

		public static final String OCS_MGR = "5";

		public static final String OCS_MON = "5";

		public static final String OCS_COM = "5";

		public static final String CRM = "60";

		public static final String DEVICE = "201";

		public static final String SUBSCRIBER = "7";
	}

	public static final class RecordType {

		public static final String INCOMING = "0";

		public static final String OUTGOING = "1";

		public static final String CALL_FORWARD = "2";

		public static final String NA = "9";

		public static final String TRANSFER = "4";

		public static final String OTHERS = "5";
	}

	public static final class CallType {

		public static final String SHORTCODE = "4";

		public static final String INTERNATIONAL = "2";

		public static final String ROAMING = "6";

		public static final String ONNET = "0";

		public static final String OFFNET = "1";

		public static final String NA = "9";

		public static final String OCS_MGR = "2";

		public static final String OCS_MON = "1";

		public static final String OCS_COM = "0";
	}

	public static final class ServiceType {

		public static final String OCS_VOICE = "0";

		public static final String OCS_SMS = "1";

		public static final String OCS_DATA = "3";

		public static final String RECHARGE = "4";

		public static final String OCS_MGR = "5";

		public static final String OCS_MON = "5";

		public static final String OCS_COM = "5";
	}

	public static final class Period {

		public static final String NIGHT = "0";

		public static final String DAY = "1";

		public static final String PEAK = "2";

		public static final String UNKNOWN = "9";
	}

	public static final class subsciberType {

		public static final String Prepaid = "0";

		public static final String POSTPAID = "1";
	}

	public static final class mVivaStatus {

		public static final String IDLE = "0";

		public static final String ACTIVE = "1";

		public static final String PRE_DEACTIVATED = "4";

		public static final int CLOSE = 5;
	}

	public static final String NCELL_COUNTRY_CODE = "977";
	public static final DateTimeFormatter PROCESSED_RECORD_DATE_FORMATTER =
			DateTimeFormat.forPattern( "CCYYMMdd HHmmss" );
	public static final String DEFAULT_COST_VALUE = "0.0000";
	public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat( "0.0000" );
	public static final int CURRENCY_DIVISOR = 100000000;
	public static final int MB_CURRENCY_DIVISOR = 1048576;
	public static final String UNKNOWN = "-1";
	public static final String EMPTY = "";
	public static final String NA = "9";
	public static final String AccountID = "-1";
	public static final String Unit = "-1";
	public static final String AccountBalance = "-1.0000";
	public static final int MSISDN_HARD_LENGTH = 13;
	public static final String TRADETYPE = "1007";
	public static boolean useCache = false;
	public static boolean containsHeader = false;
	public static String DEAULT_LANG = "English";
	public static final DateTime referenceDate = new DateTime( 2000, 01, 01, 00, 00, 00 );

}
