package com.pelatro.adaptor.ncell.ocs.voice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.pelatro.adaptor.common.ProcessedRecord;
import com.pelatro.adaptor.common.RawRecord;
import com.pelatro.adaptor.ncell.client.SubscriberServiceLookup;
import com.pelatro.adaptor.ncell.common.Constants;
import com.pelatro.adaptor.ncell.common.Constants.RecordType;
import com.pelatro.adaptor.ncell.common.Constants.RowType;
import com.pelatro.adaptor.ncell.common.RawConstants;
import com.pelatro.adaptor.ncell.common.RuleUtil;
import com.pelatro.adaptor.ncell.db.AccountDetailsLookup;
import com.pelatro.adaptor.ncell.db.AccountInfo;
import com.pelatro.adaptor.ncell.db.CellIdDetailsLookup;
import com.pelatro.adaptor.ncell.db.NumberPrefixLookup;
import com.pelatro.adaptor.ncell.db.OffnetNumberPrefixLookup;
import com.pelatro.adaptor.ncell.db.PeriodLookup;
import com.pelatro.adaptor.ncell.db.ShortCodeLookup;
import com.pelatro.adaptor.ncell.db.SubscriberIDLookup;
import com.pelatro.adaptor.ncell.db.dto.CellIdEntity;
import com.pelatro.adaptor.ncell.db.dto.CustomComparator;
import com.pelatro.adaptor.ncell.db.dto.Subscriber;

public class OCSVoiceRecord extends ProcessedRecord<OCSVoiceRecordFields, OCSVoiceRawFields> {

	private final static String FIELD_DELIM = ",";
	private final static IllegalArgumentException parseException;
	private static final String datePattern = "yyyyMMddHHmmss";
	private static final DateTimeFormatter dateFormatter =
			DateTimeFormat.forPattern( datePattern );

	private static final Map<String, String> serviceFlowToRecordType;
	private static final Pattern integerRegex = Pattern.compile( "\\d+" );
	private static final Pattern partyNumberRegex = Pattern.compile( "^[0-9*#]*$" );
	private EnumMap<OCSVoiceRecordFields, String> shortCodeRecord;

	private final RuleUtil<OCSVoiceRawFields> util;
	private NumberPrefixLookup callTypeLookup;
	private CellIdDetailsLookup cellIdLookup;
	private OffnetNumberPrefixLookup offNetLookup;
	private static ArrayList<AccountInfo> accountInfoList = new ArrayList<>();
	private boolean makeShortCodeRecord;
	private ShortCodeLookup shortCodeLookup;
	private SubscriberIDLookup subscriberIDLookup;
	private static ArrayList<Subscriber> subscriberList=new ArrayList<>();
	static {
		parseException = new IllegalArgumentException();
		serviceFlowToRecordType = new HashMap<>();

		serviceFlowToRecordType.put( RawConstants.ServiceFlow.INCOMING, RecordType.INCOMING );
		serviceFlowToRecordType.put( RawConstants.ServiceFlow.OUTGOING, RecordType.OUTGOING );
		serviceFlowToRecordType.put(
				RawConstants.ServiceFlow.CALL_FORWARD, RecordType.CALL_FORWARD );
	}

	public OCSVoiceRecord( NumberPrefixLookup callTypeLookup,
			CellIdDetailsLookup cellIdLookup, OffnetNumberPrefixLookup offNetLookup,
			ShortCodeLookup shortCodeLookup ) {
		super( OCSVoiceRecordFields.class );
		util = new RuleUtil<OCSVoiceRawFields>( parseException );
		shortCodeRecord = new EnumMap<>( OCSVoiceRecordFields.class );
		this.callTypeLookup = callTypeLookup;
		this.cellIdLookup = cellIdLookup;
		this.offNetLookup = offNetLookup;
		this.shortCodeLookup = shortCodeLookup;
		subscriberIDLookup=SubscriberIDLookup.initialize();
	}

	@Override
	public String get() {
		if ( isEmpty() )
			return null;

		String recordString = "";
		for ( String data : record.values() )
			recordString += FIELD_DELIM + data;

		return recordString.substring( 1 );
	}

	@Override
	public String getFailureReason() {
		if ( util.getFailureReason() != null )
			return util.getFailureReason();

		return super.getFailureReason();
	}

	public boolean shouldMakeComRecord() {
		return makeShortCodeRecord;
	}

	@Override
	public void read( RawRecord<OCSVoiceRawFields> rawRecord ) {
		record.clear();
		util.clear();
		failureReason = null;
		makeShortCodeRecord = false;
		if ( rawRecord == null || rawRecord.isEmpty() ) {
			failureReason = "Raw record was not parsed properly";
			return;
		}

		try {
			putRowType();
			populateUID();
			populateRecordType( rawRecord );
			populateAnumber( rawRecord );
			populateBnumber( rawRecord );
			populateServiceType();
			populateCallType( rawRecord );
			populateTimeAndSetPeak( rawRecord );
			populateCost( rawRecord );
			populateActualDuration( rawRecord );
			populateCountryCode( rawRecord );
			populateIMSI( rawRecord );
			populateCurrentBalance( rawRecord );
			populateCellId( rawRecord );
			populateCellIdDetails( rawRecord );
			populateIMEI( rawRecord );
			populateOperatorId( rawRecord );
			populateCallEndReason( rawRecord );
			populateType( rawRecord );
			populateAccountIDandBalance( rawRecord );
			populateSubscriberId(rawRecord);
			populateAnumberBnumberSwap();
		}
		catch ( IllegalArgumentException e ) {
			if ( e == parseException )
				record.clear();
			else
				throw e;
		}
	}

	private void populateAnumberBnumberSwap() {
		if ( record.get( OCSVoiceRecordFields.RECORD_TYPE )
				.equals( Constants.RecordType.INCOMING )
				|| record.get( OCSVoiceRecordFields.RECORD_TYPE )
						.equals( Constants.RecordType.CALL_FORWARD ) ) {
			String aNumber = record.get( OCSVoiceRecordFields.A_NUMBER );
			String bNumber = record.get( OCSVoiceRecordFields.B_NUMBER );
			record.put( OCSVoiceRecordFields.A_NUMBER, bNumber );
			record.put( OCSVoiceRecordFields.B_NUMBER, aNumber );
		}
	}

	private void populateAccountIDandBalance( RawRecord<OCSVoiceRawFields> rawRecord ) {
		accountInfoList.clear();
		populateAccountInfo( rawRecord );

		AccountDetailsLookup.accountIdlookup( accountInfoList );
		String accountID = AccountDetailsLookup.getAccountID();
		String unit = AccountDetailsLookup.getUnit();
		String balance_value = AccountDetailsLookup.getBalance_value();
		if ( !accountID.equals( "-1" ) ) {
			if ( unit.toUpperCase().contains( "COST" ) ) {
				balance_value = util.transformbalance( balance_value,
						OCSVoiceRawFields.CurrentAcctAmount1 );
				record.put( OCSVoiceRecordFields.ACCOUNT_TYPE1, accountID );
				record.put( OCSVoiceRecordFields.ACCOUNT_TYPE1_BALANCE, balance_value );
			}
			else if ( unit.toUpperCase().contains( "BYTE" ) ) {
				balance_value = util.transformMB( balance_value,
						OCSVoiceRawFields.CurrentAcctAmount1 );
				record.put( OCSVoiceRecordFields.ACCOUNT_TYPE1, accountID );
				record.put( OCSVoiceRecordFields.ACCOUNT_TYPE1_BALANCE, balance_value );
			}
			else {
				record.put( OCSVoiceRecordFields.ACCOUNT_TYPE1, accountID );
				record.put( OCSVoiceRecordFields.ACCOUNT_TYPE1_BALANCE, balance_value );
			}
		}
		else {
			record.put( OCSVoiceRecordFields.ACCOUNT_TYPE1, accountID );
			record.put( OCSVoiceRecordFields.ACCOUNT_TYPE1_BALANCE, balance_value );
		}

		String accountID1 = AccountDetailsLookup.getAccountID1();
		String unit1 = AccountDetailsLookup.getUnit1();
		String balance_value1 = AccountDetailsLookup.getBalance_value1();
		if ( !accountID1.equals( "-1" ) ) {
			if ( unit1.toUpperCase().contains( "COST" ) ) {
				balance_value1 = util.transformbalance( balance_value1,
						OCSVoiceRawFields.CurrentAcctAmount1 );
				record.put( OCSVoiceRecordFields.ACCOUNT_TYPE2, accountID1 );
				record.put( OCSVoiceRecordFields.ACCOUNT_TYPE2_BALANCE, balance_value1 );
			}
			else if ( unit1.toUpperCase().contains( "BYTE" ) ) {
				balance_value1 = util.transformMB( balance_value1,
						OCSVoiceRawFields.CurrentAcctAmount1 );
				record.put( OCSVoiceRecordFields.ACCOUNT_TYPE2, accountID1 );
				record.put( OCSVoiceRecordFields.ACCOUNT_TYPE2_BALANCE, balance_value1 );
			}
			else {
				record.put( OCSVoiceRecordFields.ACCOUNT_TYPE2, accountID1 );
				record.put( OCSVoiceRecordFields.ACCOUNT_TYPE2_BALANCE, balance_value1 );
			}

		}
		else {
			record.put( OCSVoiceRecordFields.ACCOUNT_TYPE2, accountID1 );
			record.put( OCSVoiceRecordFields.ACCOUNT_TYPE2_BALANCE, balance_value1 );
		}

		String accountID2 = AccountDetailsLookup.getAccountID2();
		String unit2 = AccountDetailsLookup.getUnit2();
		String balance_value2 = AccountDetailsLookup.getBalance_value2();
		if ( !accountID2.equals( "-1" ) ) {
			if ( unit2.toUpperCase().contains( "COST" ) ) {
				balance_value2 = util.transformbalance( balance_value2,
						OCSVoiceRawFields.CurrentAcctAmount1 );
				record.put( OCSVoiceRecordFields.ACCOUNT_TYPE3, accountID2 );
				record.put( OCSVoiceRecordFields.ACCOUNT_TYPE3_BALANCE, balance_value2 );
			}
			else if ( unit2.toUpperCase().contains( "BYTE" ) ) {
				balance_value2 = util.transformMB( balance_value2,
						OCSVoiceRawFields.CurrentAcctAmount1 );
				record.put( OCSVoiceRecordFields.ACCOUNT_TYPE3, accountID2 );
				record.put( OCSVoiceRecordFields.ACCOUNT_TYPE3_BALANCE, balance_value2 );
			}
			else {
				record.put( OCSVoiceRecordFields.ACCOUNT_TYPE3, accountID2 );
				record.put( OCSVoiceRecordFields.ACCOUNT_TYPE3_BALANCE, balance_value2 );
			}
		}
		else {
			record.put( OCSVoiceRecordFields.ACCOUNT_TYPE3, accountID2 );
			record.put( OCSVoiceRecordFields.ACCOUNT_TYPE3_BALANCE, balance_value2 );
		}
	}

	private void populateAccountInfo( RawRecord<OCSVoiceRawFields> rawRecord ) {
		accountInfoList.clear();
		String AccountID = Constants.EMPTY;
		String AccountBalance = Constants.EMPTY;
		if ( !rawRecord.get( OCSVoiceRawFields.AccountType1 ).trim().isEmpty()
				&& rawRecord.get( OCSVoiceRawFields.AccountType1 ).trim() != null
				&& !rawRecord.get( OCSVoiceRawFields.AccountType1 ).equals( "2000" ) ) {

			AccountInfo a1 = new AccountInfo();
			AccountID = rawRecord.get( OCSVoiceRawFields.AccountType1 );
			a1.setAccountID( Integer.parseInt( AccountID ) );
			AccountBalance =
					rawRecord.get( OCSVoiceRawFields.CurrentAcctAmount1 ).trim()
							.toString() == null
							|| rawRecord.get( OCSVoiceRawFields.CurrentAcctAmount1 ).trim()
									.toString()
									.isEmpty() ? "0"
											: rawRecord.get(
													OCSVoiceRawFields.CurrentAcctAmount1 )
													.trim().toString();
			a1.setAccountBalance( Double.parseDouble( AccountBalance ) );
			accountInfoList.add( a1 );

		}
		if ( !rawRecord.get( OCSVoiceRawFields.AccountType2 ).trim().isEmpty()
				&& rawRecord.get( OCSVoiceRawFields.AccountType2 ).trim() != null
				&& !rawRecord.get( OCSVoiceRawFields.AccountType2 ).equals( "2000" ) ) {
			AccountInfo a1 = new AccountInfo();
			AccountID = rawRecord.get( OCSVoiceRawFields.AccountType2 );
			a1.setAccountID( Integer.parseInt( AccountID ) );
			AccountBalance =
					rawRecord.get( OCSVoiceRawFields.CurrentAcctAmount2 ).trim()
							.toString() == null
							|| rawRecord.get( OCSVoiceRawFields.CurrentAcctAmount2 ).trim()
									.toString()
									.isEmpty() ? "0"
											: rawRecord.get(
													OCSVoiceRawFields.CurrentAcctAmount2 )
													.trim().toString();
			a1.setAccountBalance( Double.parseDouble( AccountBalance ) );
			accountInfoList.add( a1 );
		}
		if ( !rawRecord.get( OCSVoiceRawFields.AccountType3 ).trim().isEmpty()
				&& rawRecord.get( OCSVoiceRawFields.AccountType3 ).trim() != null
				&& !rawRecord.get( OCSVoiceRawFields.AccountType3 ).equals( "2000" ) ) {
			AccountInfo a1 = new AccountInfo();
			AccountID = rawRecord.get( OCSVoiceRawFields.AccountType3 );
			a1.setAccountID( Integer.parseInt( AccountID ) );
			AccountBalance =
					rawRecord.get( OCSVoiceRawFields.CurrentAcctAmount3 ).trim()
							.toString() == null
							|| rawRecord.get( OCSVoiceRawFields.CurrentAcctAmount3 ).trim()
									.toString()
									.isEmpty() ? "0"
											: rawRecord.get(
													OCSVoiceRawFields.CurrentAcctAmount3 )
													.trim().toString();
			a1.setAccountBalance( Double.parseDouble( AccountBalance ) );
			accountInfoList.add( a1 );
		}
		if ( !rawRecord.get( OCSVoiceRawFields.AccountType4 ).isEmpty()
				&& rawRecord.get( OCSVoiceRawFields.AccountType4 ) != null
				&& !rawRecord.get( OCSVoiceRawFields.AccountType4 ).equals( "2000" ) ) {
			AccountInfo a1 = new AccountInfo();
			AccountID = rawRecord.get( OCSVoiceRawFields.AccountType4 );
			a1.setAccountID( Integer.parseInt( AccountID ) );
			AccountBalance =
					rawRecord.get( OCSVoiceRawFields.CurrentAcctAmount4 ).trim()
							.toString() == null
							|| rawRecord.get( OCSVoiceRawFields.CurrentAcctAmount4 ).trim()
									.toString()
									.isEmpty() ? "0"
											: rawRecord.get(
													OCSVoiceRawFields.CurrentAcctAmount4 )
													.trim().toString();
			a1.setAccountBalance( Double.parseDouble( AccountBalance ) );
			accountInfoList.add( a1 );
		}
		if ( !rawRecord.get( OCSVoiceRawFields.AccountType5 ).isEmpty()
				&& rawRecord.get( OCSVoiceRawFields.AccountType5 ) != null
				&& !rawRecord.get( OCSVoiceRawFields.AccountType5 ).equals( "2000" ) ) {
			AccountInfo a1 = new AccountInfo();
			AccountID = rawRecord.get( OCSVoiceRawFields.AccountType5 );
			a1.setAccountID( Integer.parseInt( AccountID ) );
			AccountBalance =
					rawRecord.get( OCSVoiceRawFields.CurrentAcctAmount5 ).trim()
							.toString() == null
							|| rawRecord.get( OCSVoiceRawFields.CurrentAcctAmount5 ).trim()
									.toString()
									.isEmpty() ? "0"
											: rawRecord.get(
													OCSVoiceRawFields.CurrentAcctAmount5 )
													.trim().toString();
			a1.setAccountBalance( Double.parseDouble( AccountBalance ) );
			accountInfoList.add( a1 );
		}
		if ( !rawRecord.get( OCSVoiceRawFields.AccountType6 ).isEmpty()
				&& rawRecord.get( OCSVoiceRawFields.AccountType6 ) != null
				&& !rawRecord.get( OCSVoiceRawFields.AccountType6 ).equals( "2000" ) ) {
			AccountInfo a1 = new AccountInfo();
			AccountID = rawRecord.get( OCSVoiceRawFields.AccountType6 );
			a1.setAccountID( Integer.parseInt( AccountID ) );
			AccountBalance =
					rawRecord.get( OCSVoiceRawFields.CurrentAcctAmount6 ).trim()
							.toString() == null
							|| rawRecord.get( OCSVoiceRawFields.CurrentAcctAmount6 ).trim()
									.toString()
									.isEmpty() ? "0"
											: rawRecord.get(
													OCSVoiceRawFields.CurrentAcctAmount6 )
													.trim().toString();
			a1.setAccountBalance( Double.parseDouble( AccountBalance ) );
			accountInfoList.add( a1 );
		}
		if ( !rawRecord.get( OCSVoiceRawFields.AccountType7 ).isEmpty()
				&& rawRecord.get( OCSVoiceRawFields.AccountType7 ) != null
				&& !rawRecord.get( OCSVoiceRawFields.AccountType7 ).equals( "2000" ) ) {
			AccountInfo a1 = new AccountInfo();
			AccountID = rawRecord.get( OCSVoiceRawFields.AccountType7 );
			a1.setAccountID( Integer.parseInt( AccountID ) );
			AccountBalance =
					rawRecord.get( OCSVoiceRawFields.CurrentAcctAmount7 ).trim()
							.toString() == null
							|| rawRecord.get( OCSVoiceRawFields.CurrentAcctAmount7 ).trim()
									.toString()
									.isEmpty() ? "0"
											: rawRecord.get(
													OCSVoiceRawFields.CurrentAcctAmount7 )
													.trim().toString();
			a1.setAccountBalance( Double.parseDouble( AccountBalance ) );
			accountInfoList.add( a1 );
		}
		if ( !rawRecord.get( OCSVoiceRawFields.AccountType8 ).isEmpty()
				&& rawRecord.get( OCSVoiceRawFields.AccountType8 ) != null
				&& !rawRecord.get( OCSVoiceRawFields.AccountType8 ).equals( "2000" ) ) {
			AccountInfo a1 = new AccountInfo();
			AccountID = rawRecord.get( OCSVoiceRawFields.AccountType8 );
			a1.setAccountID( Integer.parseInt( AccountID ) );
			AccountBalance =
					rawRecord.get( OCSVoiceRawFields.CurrentAcctAmount8 ).trim()
							.toString() == null
							|| rawRecord.get( OCSVoiceRawFields.CurrentAcctAmount8 ).trim()
									.toString()
									.isEmpty() ? "0"
											: rawRecord.get(
													OCSVoiceRawFields.CurrentAcctAmount8 )
													.trim().toString();
			a1.setAccountBalance( Double.parseDouble( AccountBalance ) );
			accountInfoList.add( a1 );
		}

		if ( !rawRecord.get( OCSVoiceRawFields.AccountType9 ).isEmpty()
				&& rawRecord.get( OCSVoiceRawFields.AccountType9 ) != null
				&& !rawRecord.get( OCSVoiceRawFields.AccountType9 ).equals( "2000" ) ) {
			AccountInfo a1 = new AccountInfo();
			AccountID = rawRecord.get( OCSVoiceRawFields.AccountType9 );
			a1.setAccountID( Integer.parseInt( AccountID ) );
			AccountBalance =
					rawRecord.get( OCSVoiceRawFields.CurrentAcctAmount9 ).trim()
							.toString() == null
							|| rawRecord.get( OCSVoiceRawFields.CurrentAcctAmount9 ).trim()
									.toString()
									.isEmpty() ? "0"
											: rawRecord.get(
													OCSVoiceRawFields.CurrentAcctAmount9 )
													.trim().toString();
			a1.setAccountBalance( Double.parseDouble( AccountBalance ) );
			accountInfoList.add( a1 );
		}
		if ( !rawRecord.get( OCSVoiceRawFields.AccountType10 ).isEmpty()
				&& rawRecord.get( OCSVoiceRawFields.AccountType10 ) != null
				&& !rawRecord.get( OCSVoiceRawFields.AccountType10 ).equals( "2000" ) ) {
			AccountInfo a1 = new AccountInfo();
			AccountID = rawRecord.get( OCSVoiceRawFields.AccountType10 );
			a1.setAccountID( Integer.parseInt( AccountID ) );
			AccountBalance =
					rawRecord.get( OCSVoiceRawFields.CurrentAcctAmount10 ).trim()
							.toString() == null
							|| rawRecord.get( OCSVoiceRawFields.CurrentAcctAmount10 ).trim()
									.toString()
									.isEmpty() ? "0"
											: rawRecord.get(
													OCSVoiceRawFields.CurrentAcctAmount10 )
													.trim().toString();
			a1.setAccountBalance( Double.parseDouble( AccountBalance ) );
			accountInfoList.add( a1 );
		}
		Collections.sort( accountInfoList, new CustomComparator() );
		Collections.reverse( accountInfoList );
	}

	private void populateSubscriberId(RawRecord<OCSVoiceRawFields> rawRecord) {
		subscriberList.clear();
		String raw=rawRecord.get(OCSVoiceRawFields.CallingPartyNumber);
		if ( raw == null || raw.isEmpty() || raw.equalsIgnoreCase( "NULL" ) )
			raw = Constants.NA;
		String subscriberid=Integer.toString(subscriberIDLookup.getSubscriberID(raw));
		record.put(OCSVoiceRecordFields.SUBSCRIBER_ID,subscriberid);
	}

	private void populateUID() {
		record.put( OCSVoiceRecordFields.UID,
				SubscriberServiceLookup.bindParamUId );
	}

	private void populateServiceType() {
		record.put( OCSVoiceRecordFields.SERVICE_TYPE, Constants.ServiceType.OCS_VOICE );
	}

	private void populateType( RawRecord<OCSVoiceRawFields> rawRecord ) {
		String raw = rawRecord.get( OCSVoiceRawFields.PayType );
		if ( raw == null || raw.isEmpty() || raw.equalsIgnoreCase( "NULL" ) )
			raw = Constants.NA;
		record.put( OCSVoiceRecordFields.TYPE, raw );
	}

	private void populateCallEndReason( RawRecord<OCSVoiceRawFields> rawRecord ) {
		String raw = rawRecord.get( OCSVoiceRawFields.TerminationReason );
		if ( raw == null || raw.isEmpty() || raw.equalsIgnoreCase( "NULL" ) )
			raw = Constants.NA;
		record.put( OCSVoiceRecordFields.CALL_END_REASON, raw );
	}

	private void populateOperatorId( RawRecord<OCSVoiceRawFields> rawRecord ) {
		String bnumber = getOwnerMsisdn();
		String operatorid =
				String.valueOf( callTypeLookup.operatorIdlookup( bnumber ) );
		if ( operatorid.equals( "9" ) ) {
			String offNetOperatorId =
					String.valueOf( offNetLookup.operatorIdlookup( bnumber ) );
			record.put( OCSVoiceRecordFields.OPERATOR_ID, offNetOperatorId );
			return;
		}
		record.put( OCSVoiceRecordFields.OPERATOR_ID, operatorid );
	}

	private void populateIMEI( RawRecord<OCSVoiceRawFields> rawRecord ) {
		String raw = rawRecord.get( OCSVoiceRawFields.IMEI );
		raw = util.emptyCheck( raw );
		record.put( OCSVoiceRecordFields.IMEI, raw );
	}

	private void populateCellIdDetails( RawRecord<OCSVoiceRawFields> rawRecord ) {
		String cellId = rawRecord.get( OCSVoiceRawFields.CallingCellID );
		if ( cellId.equals( null ) || cellId.isEmpty() || cellId.equalsIgnoreCase( "NULL" ) ) {
			record.put( OCSVoiceRecordFields.PROVINCE, Constants.EMPTY );
			record.put( OCSVoiceRecordFields.DISTRICT, Constants.EMPTY );
			record.put( OCSVoiceRecordFields.AREA, Constants.EMPTY );
		}
		else {
			cellId = cellId.substring( 5, cellId.length() );
			CellIdEntity cellIdDetails = cellIdLookup.getCellIdDetails( cellId );
			if ( cellIdDetails == null ) {
				record.put( OCSVoiceRecordFields.PROVINCE, Constants.EMPTY );
				record.put( OCSVoiceRecordFields.DISTRICT, Constants.EMPTY );
				record.put( OCSVoiceRecordFields.AREA, Constants.EMPTY );
			}
			else {
				record.put( OCSVoiceRecordFields.PROVINCE,
						cellIdDetails.getProvince() == null ? ""
								: cellIdDetails.getProvince() );
				record.put( OCSVoiceRecordFields.DISTRICT,
						cellIdDetails.getDistrict() == null ? ""
								: cellIdDetails.getDistrict() );
				record.put( OCSVoiceRecordFields.AREA,
						cellIdDetails.getArea() == null ? ""
								: cellIdDetails.getArea() );
			}
		}
	}

	private void populateCellId( RawRecord<OCSVoiceRawFields> rawRecord ) {
		String raw = rawRecord.get( OCSVoiceRawFields.CallingCellID );
		raw = util.emptyCheck( raw );
		record.put( OCSVoiceRecordFields.CELL_ID, raw );
	}

	private void populateCurrentBalance( RawRecord<OCSVoiceRawFields> rawRecord ) {
		String prepaidBalance = rawRecord.get( OCSVoiceRawFields.PrepaidBalance );
		if ( prepaidBalance.isEmpty() || prepaidBalance.equals( null )
				|| prepaidBalance.equalsIgnoreCase( "NULL" ) ) {
			record.put( OCSVoiceRecordFields.CURRENT_BALANCE, Constants.DEFAULT_COST_VALUE );
		}
		else {
			prepaidBalance = util.transformAmount( prepaidBalance,
					OCSVoiceRawFields.PrepaidBalance );
			record.put( OCSVoiceRecordFields.CURRENT_BALANCE, prepaidBalance );
		}

	}

	private void populateIMSI( RawRecord<OCSVoiceRawFields> rawRecord ) {
		String raw = rawRecord.get( OCSVoiceRawFields.CallingPartyIMSI );
		raw = util.emptyCheck( raw );
		raw = util.matchNonStrictPattern( partyNumberRegex, raw,
				OCSVoiceRawFields.CallingPartyIMSI );
		record.put( OCSVoiceRecordFields.IMSI, raw );
	}

	private void populateCountryCode( RawRecord<OCSVoiceRawFields> rawRecord ) {
		String raw = rawRecord.get( OCSVoiceRawFields.CalledHomeCountryCode );

		if ( raw.isEmpty() || "-1".equals( raw ) ) {
			raw = Constants.UNKNOWN;
		}
		else {
			if ( !integerRegex.matcher( raw ).matches() ) {
				failureReason = String.format( "%s is not a non-negative integer: %s",
						OCSVoiceRawFields.CalledHomeCountryCode.name(), raw );
				throw parseException;
			}

			int countryCode = Integer.parseInt( raw );

			if ( countryCode == 0 || countryCode == 9999 ) {
				raw = Constants.UNKNOWN;
			}
		}
		record.put( OCSVoiceRecordFields.COUNTRY_CODE, raw );
	}

	private void populateActualDuration( RawRecord<OCSVoiceRawFields> rawRecord ) {
		String raw = rawRecord.get( OCSVoiceRawFields.CallDuration );
		raw = util.matchStrictPattern( integerRegex, raw, OCSVoiceRawFields.CallDuration );
		record.put( OCSVoiceRecordFields.ACTUAL_DURATION, raw );
	}

	private void populateCost( RawRecord<OCSVoiceRawFields> rawRecord ) {
		String raw = rawRecord.get( OCSVoiceRawFields.ChargeFromPrepaid );
		if ( raw.equals( null ) || raw.isEmpty() || raw.equalsIgnoreCase( "NULL" ) ) {
			record.put( OCSVoiceRecordFields.COST, Constants.DEFAULT_COST_VALUE );
		}
		else {
			raw = util.transformAmount( raw, OCSVoiceRawFields.ChargeFromPrepaid );
			record.put( OCSVoiceRecordFields.COST, raw );
		}
	}

	private void populateTimeAndSetPeak( RawRecord<OCSVoiceRawFields> rawRecord ) {
		String raw = rawRecord.get( OCSVoiceRawFields.ChargingTime );
		DateTime startTime = util.extractDate( dateFormatter, datePattern, raw,
				OCSVoiceRawFields.ChargingTime );
		record.put( OCSVoiceRecordFields.START_TIME,
				Constants.PROCESSED_RECORD_DATE_FORMATTER.print( startTime ) );
		record.put( OCSVoiceRecordFields.PEAK, PeriodLookup.lookup( startTime ) );
	}

	private void populateCallType( RawRecord<OCSVoiceRawFields> rawRecord ) {

		if ( rawRecord.get( OCSVoiceRawFields.RoamState )
				.equals( RawConstants.CallType.ROAMSTATE ) ) {
			record.put( OCSVoiceRecordFields.CALL_TYPE, Constants.CallType.ROAMING );
		}
		else if ( rawRecord.get( OCSVoiceRawFields.CallType )
				.equals( RawConstants.CallType.CALLTYPE ) ) {
			record.put( OCSVoiceRecordFields.CALL_TYPE, Constants.CallType.INTERNATIONAL );
		}
		else if ( getOwnerMsisdn().substring( 3, getOwnerMsisdn().length() ).length() <= 6 ) {
			record.put( OCSVoiceRecordFields.CALL_TYPE, Constants.CallType.SHORTCODE );
		}
		else {
			String bnumber = getOwnerMsisdn();
			String CallType = String.valueOf( callTypeLookup.callTypelookup( bnumber ) );
			if ( CallType.equals( "9" ) ) {
				String offNetCallType =
						String.valueOf( offNetLookup.callTypelookup( bnumber ) );
				record.put( OCSVoiceRecordFields.CALL_TYPE, offNetCallType );
				return;
			}
			record.put( OCSVoiceRecordFields.CALL_TYPE, CallType );
		}
	}

	private void putRowType() {
		record.put( OCSVoiceRecordFields.ROW_TYPE, RowType.OCS_VOICE );
	}

	private void populateRecordType( RawRecord<OCSVoiceRawFields> rawRecord ) {
		String raw = rawRecord.get( OCSVoiceRawFields.ServiceFlow );
		String recordType = util.matchNonStrictIn(
				serviceFlowToRecordType, raw,
				OCSVoiceRawFields.ServiceFlow, RecordType.NA );
		record.put( OCSVoiceRecordFields.RECORD_TYPE, recordType );
	}

	private void populateBnumber( RawRecord<OCSVoiceRawFields> rawRecord ) {
		String raw = rawRecord.get( OCSVoiceRawFields.CalledPartyNumber );
		raw = util.matchStrictPattern(
				partyNumberRegex, raw, OCSVoiceRawFields.CalledPartyNumber );
		if ( shortCodeLookup.isShortCode( raw ) ) {
			makeShortCodeRecord = true;
		}
		if ( !RecordType.OUTGOING.equals( record.get( OCSVoiceRecordFields.RECORD_TYPE ) ) ) {

			raw = util.addCountryCode( raw );

		}
		record.put( OCSVoiceRecordFields.B_NUMBER, raw );
	}

	private void populateAnumber( RawRecord<OCSVoiceRawFields> rawRecord ) {
		String raw = rawRecord.get( OCSVoiceRawFields.CallingPartyNumber );
		raw = util.matchStrictPattern(
				partyNumberRegex, raw, OCSVoiceRawFields.CallingPartyNumber );

		if ( RecordType.OUTGOING.equals( record.get( OCSVoiceRecordFields.RECORD_TYPE ) ) ) {

			raw = util.addCountryCode( raw );

		}
		record.put( OCSVoiceRecordFields.A_NUMBER, raw );
	}

	public String getOwnerMsisdn() {
		return RecordType.OUTGOING.equals( record.get( OCSVoiceRecordFields.RECORD_TYPE ) )
				? record.get( OCSVoiceRecordFields.B_NUMBER )
				: record.get( OCSVoiceRecordFields.A_NUMBER );
	}

	public String getMsisdn() {
		return RecordType.CALL_FORWARD.equals( record.get( OCSVoiceRecordFields.RECORD_TYPE ) )
				? record.get( OCSVoiceRecordFields.B_NUMBER )
				: record.get( OCSVoiceRecordFields.A_NUMBER );
	}

	public String makeShortCodeRecord() {
		shortCodeRecord.put( OCSVoiceRecordFields.ROW_TYPE,
				record.get( OCSVoiceRecordFields.ROW_TYPE ) );
		shortCodeRecord.put( OCSVoiceRecordFields.UID, SubscriberServiceLookup.bindParamUId );
		shortCodeRecord.put( OCSVoiceRecordFields.A_NUMBER,
				record.get( OCSVoiceRecordFields.A_NUMBER ) );
		shortCodeRecord.put( OCSVoiceRecordFields.B_NUMBER,
				record.get( OCSVoiceRecordFields.B_NUMBER ) );
		shortCodeRecord.put( OCSVoiceRecordFields.SERVICE_TYPE,
				record.get( OCSVoiceRecordFields.SERVICE_TYPE ) );
		shortCodeRecord.put( OCSVoiceRecordFields.RECORD_TYPE,
				record.get( OCSVoiceRecordFields.RECORD_TYPE ) );
		shortCodeRecord.put( OCSVoiceRecordFields.CALL_TYPE,
				record.get( OCSVoiceRecordFields.CALL_TYPE ) );
		shortCodeRecord.put( OCSVoiceRecordFields.START_TIME,
				record.get( OCSVoiceRecordFields.START_TIME ) );
		shortCodeRecord.put( OCSVoiceRecordFields.PEAK,
				record.get( OCSVoiceRecordFields.PEAK ) );
		shortCodeRecord.put( OCSVoiceRecordFields.COST,
				record.get( OCSVoiceRecordFields.COST ) );
		shortCodeRecord.put( OCSVoiceRecordFields.ACTUAL_DURATION,
				record.get( OCSVoiceRecordFields.ACTUAL_DURATION ) );
		shortCodeRecord.put( OCSVoiceRecordFields.COUNTRY_CODE,
				record.get( OCSVoiceRecordFields.COUNTRY_CODE ) );
		shortCodeRecord.put( OCSVoiceRecordFields.IMSI,
				record.get( OCSVoiceRecordFields.IMSI ) );
		shortCodeRecord.put( OCSVoiceRecordFields.CURRENT_BALANCE,
				record.get( OCSVoiceRecordFields.CURRENT_BALANCE ) );
		shortCodeRecord.put( OCSVoiceRecordFields.CELL_ID,
				record.get( OCSVoiceRecordFields.CELL_ID ) );
		shortCodeRecord.put( OCSVoiceRecordFields.PROVINCE,
				record.get( OCSVoiceRecordFields.PROVINCE ) );
		shortCodeRecord.put( OCSVoiceRecordFields.DISTRICT,
				record.get( OCSVoiceRecordFields.DISTRICT ) );
		shortCodeRecord.put( OCSVoiceRecordFields.AREA,
				record.get( OCSVoiceRecordFields.AREA ) );
		shortCodeRecord.put( OCSVoiceRecordFields.IMEI,
				record.get( OCSVoiceRecordFields.IMEI ) );
		shortCodeRecord.put( OCSVoiceRecordFields.OPERATOR_ID,
				record.get( OCSVoiceRecordFields.OPERATOR_ID ) );
		shortCodeRecord.put( OCSVoiceRecordFields.CALL_END_REASON,
				record.get( OCSVoiceRecordFields.CALL_END_REASON ) );
		shortCodeRecord.put( OCSVoiceRecordFields.TYPE,
				record.get( OCSVoiceRecordFields.TYPE ) );
		shortCodeRecord.put( OCSVoiceRecordFields.ACCOUNT_TYPE1,
				record.get( OCSVoiceRecordFields.ACCOUNT_TYPE1 ) );
		shortCodeRecord.put( OCSVoiceRecordFields.ACCOUNT_TYPE1_BALANCE,
				record.get( OCSVoiceRecordFields.ACCOUNT_TYPE1_BALANCE ) );
		shortCodeRecord.put( OCSVoiceRecordFields.ACCOUNT_TYPE2,
				record.get( OCSVoiceRecordFields.ACCOUNT_TYPE2 ) );
		shortCodeRecord.put( OCSVoiceRecordFields.ACCOUNT_TYPE2_BALANCE,
				record.get( OCSVoiceRecordFields.ACCOUNT_TYPE2_BALANCE ) );
		shortCodeRecord.put( OCSVoiceRecordFields.ACCOUNT_TYPE3,
				record.get( OCSVoiceRecordFields.ACCOUNT_TYPE3 ) );
		shortCodeRecord.put( OCSVoiceRecordFields.ACCOUNT_TYPE3_BALANCE,
				record.get( OCSVoiceRecordFields.ACCOUNT_TYPE3_BALANCE ) );
		shortCodeRecord.put( OCSVoiceRecordFields.SUBSCRIBER_ID,
				SubscriberServiceLookup.bindParamSubscriberId );
		String recordString = "";
		for ( String data : shortCodeRecord.values() )
			recordString += FIELD_DELIM + data;
		
		return recordString.substring( 1 );
	}
}
