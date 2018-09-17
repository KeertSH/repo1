package com.pelatro.adaptor.ncell.ocs.voice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.EnumMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.pelatro.adaptor.common.RawRecord;
import com.pelatro.adaptor.ncell.common.Constants;
import com.pelatro.adaptor.ncell.common.Constants.RowType;
import com.pelatro.adaptor.ncell.common.RawConstants;
import com.pelatro.adaptor.ncell.db.CellIdDetailsLookup;
import com.pelatro.adaptor.ncell.db.NumberPrefixLookup;
import com.pelatro.adaptor.ncell.db.OffnetNumberPrefixLookup;
import com.pelatro.adaptor.ncell.db.ShortCodeLookup;
import com.pelatro.adaptor.ncell.db.UniqueIDGenerator;
import com.pelatro.adaptor.ncell.db.dto.CellIdEntity;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

@RunWith( JMockit.class )
public class OCSVoiceRecordTest {

	private OCSVoiceRecord record;
	private EnumMap<OCSVoiceRawFields, String> fields;

	@Mocked UniqueIDGenerator mockUuid;
	@Mocked NumberPrefixLookup mockCallType;
	@Mocked CellIdDetailsLookup mockCellId;
	@Mocked OffnetNumberPrefixLookup mockOffnetLookup;
	@Mocked ShortCodeLookup mockShortCodeLookup;

	@Before
	public void testSetUp() {
		new MockUp<UniqueIDGenerator>() {

			@Mock
			public void open() {
			}

			@Mock
			public long generate() {
				return 9876l;
			}
		};
		fields = getRequiredInputFields();
		record = new OCSVoiceRecord( mockCallType, mockCellId, mockOffnetLookup,
			mockShortCodeLookup );
	}

	@Test
	public void testNullRecord() {
		record.read( new RawRecord<>( OCSVoiceRawFields.class, "" ) );
		assertNull( record.get() );
		assertTrue( record.isEmpty() );
		assertEquals( "Raw record was not parsed properly", record.getFailureReason() );
	}

	@Test
	public void testEmptyRecord() {
		record.read( null );
		assertNull( record.get() );
		assertTrue( record.isEmpty() );
		assertEquals( "Raw record was not parsed properly", record.getFailureReason() );
	}

	@Test
	public void testRowType() {
		record.read( makeRawRecord( fields ) );
		assertFalse( record.isEmpty() );
		assertEquals( RowType.OCS_VOICE, record.get( OCSVoiceRecordFields.ROW_TYPE ) );
	}

	@Test
	public void testServiceType() {
		record.read( makeRawRecord( fields ) );
		assertFalse( record.isEmpty() );
		assertEquals( Constants.ServiceType.OCS_VOICE,
				record.get( OCSVoiceRecordFields.SERVICE_TYPE ) );
	}

	@Test
	public void testRecordType() {
		fields = getRequiredInputFields();
		fields.put( OCSVoiceRawFields.ServiceFlow, RawConstants.ServiceFlow.INCOMING );
		record.read( makeRawRecord( fields ) );
		assertEquals( Constants.RecordType.INCOMING,
				record.get( OCSVoiceRecordFields.RECORD_TYPE ) );

		fields.put( OCSVoiceRawFields.ServiceFlow, RawConstants.ServiceFlow.OUTGOING );
		record.read( makeRawRecord( fields ) );
		assertEquals( Constants.RecordType.OUTGOING,
				record.get( OCSVoiceRecordFields.RECORD_TYPE ) );

		fields.put( OCSVoiceRawFields.ServiceFlow, RawConstants.ServiceFlow.CALL_FORWARD );
		record.read( makeRawRecord( fields ) );
		assertEquals( Constants.RecordType.CALL_FORWARD,
				record.get( OCSVoiceRecordFields.RECORD_TYPE ) );

		fields.put( OCSVoiceRawFields.ServiceFlow, "" );
		record.read( makeRawRecord( fields ) );
		assertEquals( Constants.RecordType.NA,
				record.get( OCSVoiceRecordFields.RECORD_TYPE ) );

		fields.put( OCSVoiceRawFields.ServiceFlow, null );
		record.read( makeRawRecord( fields ) );
		assertEquals( Constants.RecordType.NA,
				record.get( OCSVoiceRecordFields.RECORD_TYPE ) );

		fields.put( OCSVoiceRawFields.ServiceFlow, "NULL" );
		record.read( makeRawRecord( fields ) );
		assertEquals( Constants.RecordType.NA,
				record.get( OCSVoiceRecordFields.RECORD_TYPE ) );
	}

	@Test
	public void testCallType() {
		fields = getRequiredInputFields();

		fields.put( OCSVoiceRawFields.RoamState, RawConstants.CallType.ROAMSTATE );
		record.read( makeRawRecord( fields ) );
		assertEquals( Constants.CallType.ROAMING,
				record.get( OCSVoiceRecordFields.CALL_TYPE ) );

		fields.put( OCSVoiceRawFields.RoamState, "4" );
		fields.put( OCSVoiceRawFields.CallType, RawConstants.CallType.CALLTYPE );
		record.read( makeRawRecord( fields ) );
		assertEquals( Constants.CallType.INTERNATIONAL,
				record.get( OCSVoiceRecordFields.CALL_TYPE ) );

		fields.put( OCSVoiceRawFields.ServiceFlow, RawConstants.ServiceFlow.OUTGOING );
		fields.put( OCSVoiceRawFields.RoamState, "4" );
		fields.put( OCSVoiceRawFields.CallType, "2" );
		fields.put( OCSVoiceRawFields.CalledPartyNumber, "977123458" );
		record.read( makeRawRecord( fields ) );
		assertEquals( Constants.CallType.SHORTCODE,
				record.get( OCSVoiceRecordFields.CALL_TYPE ) );

		new Expectations() {

			{
				mockCallType.callTypelookup( anyString );
				result = 1;
			}
		};
		fields.put( OCSVoiceRawFields.ServiceFlow, RawConstants.ServiceFlow.OUTGOING );
		fields.put( OCSVoiceRawFields.RoamState, "4" );
		fields.put( OCSVoiceRawFields.CallType, "2" );
		fields.put( OCSVoiceRawFields.CalledPartyNumber, "97712345812" );
		fields.put( OCSVoiceRawFields.UsageServiceType, "1" );
		record.read( makeRawRecord( fields ) );
		assertEquals( Constants.CallType.OFFNET,
				record.get( OCSVoiceRecordFields.CALL_TYPE ) );
	}

	@Test
	public void testOperatorId() {
		fields = getRequiredInputFields();
		new Expectations() {

			{
				mockCallType.operatorIdlookup( anyString );
				result = 1;
			}
		};
		fields.put( OCSVoiceRawFields.UsageServiceType, "12" );
		record.read( makeRawRecord( fields ) );
		assertEquals( "1", record.get( OCSVoiceRecordFields.OPERATOR_ID ) );
	}

	@Test
	public void testTimeStamp() {
		fields = getRequiredInputFields();
		fields.put( OCSVoiceRawFields.ChargingTime, "" );
		record.read( makeRawRecord( fields ) );
		assertNull( record.get() );
		assertEquals( "Required field ChargingTime is missing",
				record.getFailureReason() );

		fields.put( OCSVoiceRawFields.ChargingTime, "201512123699999" );
		record.read( makeRawRecord( fields ) );
		assertNull( record.get() );
		assertEquals(
				"ChargingTime does not conform to 'yyyyMMddHHmmss' : 201512123699999",
				record.getFailureReason() );

		fields.put( OCSVoiceRawFields.ChargingTime, "20150707123500" );
		record.read( makeRawRecord( fields ) );
		assertEquals( "20150707 123500", record.get( OCSVoiceRecordFields.START_TIME ) );

		fields.put( OCSVoiceRawFields.ChargingTime, "20150707183500" );
		record.read( makeRawRecord( fields ) );
		assertEquals( "20150707 183500", record.get( OCSVoiceRecordFields.START_TIME ) );

		fields.put( OCSVoiceRawFields.ChargingTime, "20150707165959" );
		record.read( makeRawRecord( fields ) );
		assertEquals( "20150707 165959", record.get( OCSVoiceRecordFields.START_TIME ) );
	}

	@Test
	public void testCost() {
		fields = getRequiredInputFields();
		fields.put( OCSVoiceRawFields.ChargeFromPrepaid, "" );
		record.read( makeRawRecord( fields ) );
		assertEquals( Constants.DEFAULT_COST_VALUE, record.get( OCSVoiceRecordFields.COST ) );

		fields.put( OCSVoiceRawFields.ChargeFromPrepaid, null );
		record.read( makeRawRecord( fields ) );
		assertEquals( Constants.DEFAULT_COST_VALUE, record.get( OCSVoiceRecordFields.COST ) );

		fields.put( OCSVoiceRawFields.ChargeFromPrepaid, "NULL" );
		record.read( makeRawRecord( fields ) );
		assertEquals( Constants.DEFAULT_COST_VALUE, record.get( OCSVoiceRecordFields.COST ) );

		fields.put( OCSVoiceRawFields.ChargeFromPrepaid, "12345" );
		record.read( makeRawRecord( fields ) );
		assertEquals( "0.0001", record.get( OCSVoiceRecordFields.COST ) );
	}

	@Test
	public void testExtractDuration() {
		fields = getRequiredInputFields();
		fields.put( OCSVoiceRawFields.CallDuration, "" );
		record.read( makeRawRecord( fields ) );
		fields.put( OCSVoiceRawFields.CallDuration, "abc" );
		record.read( makeRawRecord( fields ) );

		assertNull( record.get() );
		assertEquals( "CallDuration should conform to pattern \\d+ : abc",
				record.getFailureReason() );

		fields.put( OCSVoiceRawFields.CallDuration, "12-20" );
		record.read( makeRawRecord( fields ) );

		assertNull( record.get() );
		assertEquals( "CallDuration should conform to pattern \\d+ : 12-20",
				record.getFailureReason() );

		fields.put( OCSVoiceRawFields.CallDuration, "-0" );
		record.read( makeRawRecord( fields ) );

		assertNull( record.get() );
		assertEquals( "CallDuration should conform to pattern \\d+ : -0",
				record.getFailureReason() );

		fields.put( OCSVoiceRawFields.CallDuration, "12.34" );
		record.read( makeRawRecord( fields ) );

		assertNull( record.get() );
		assertEquals( "CallDuration should conform to pattern \\d+ : 12.34",
				record.getFailureReason() );

		fields.put( OCSVoiceRawFields.CallDuration, "40" );

		record.read( makeRawRecord( fields ) );
		assertEquals( "40", record.get( OCSVoiceRecordFields.ACTUAL_DURATION ) );
	}

	@Test
	public void testExtractCountryCode() {
		fields = getRequiredInputFields();
		fields.put( OCSVoiceRawFields.CalledHomeCountryCode, "" );
		record.read( makeRawRecord( fields ) );
		assertEquals( "-1", record.get( OCSVoiceRecordFields.COUNTRY_CODE ) );

		fields.put( OCSVoiceRawFields.CalledHomeCountryCode, "-1" );
		record.read( makeRawRecord( fields ) );
		assertEquals( "-1", record.get( OCSVoiceRecordFields.COUNTRY_CODE ) );

		fields.put( OCSVoiceRawFields.CalledHomeCountryCode, "0" );
		record.read( makeRawRecord( fields ) );
		assertEquals( "-1", record.get( OCSVoiceRecordFields.COUNTRY_CODE ) );

		fields.put( OCSVoiceRawFields.CalledHomeCountryCode, "9999" );
		record.read( makeRawRecord( fields ) );
		assertEquals( "-1", record.get( OCSVoiceRecordFields.COUNTRY_CODE ) );

		fields.put( OCSVoiceRawFields.CalledHomeCountryCode, "4" );
		record.read( makeRawRecord( fields ) );
		assertEquals( "4", record.get( OCSVoiceRecordFields.COUNTRY_CODE ) );

		fields.put( OCSVoiceRawFields.CalledHomeCountryCode, "-4" );
		record.read( makeRawRecord( fields ) );
		assertNull( record.get() );
		assertEquals( "CalledHomeCountryCode is not a non-negative integer: -4",
				record.getFailureReason() );

		fields.put( OCSVoiceRawFields.CalledHomeCountryCode, "4.5" );
		record.read( makeRawRecord( fields ) );
		assertNull( record.get() );
		assertEquals( "CalledHomeCountryCode is not a non-negative integer: 4.5",
				record.getFailureReason() );
	}

	@Test
	public void testExtractIMSI() {
		fields = getRequiredInputFields();
		fields.put( OCSVoiceRawFields.CallingPartyIMSI, "" );
		record.read( makeRawRecord( fields ) );
		assertEquals( "", record.get( OCSVoiceRecordFields.IMSI ) );

		fields.put( OCSVoiceRawFields.CallingPartyIMSI, "543" );
		record.read( makeRawRecord( fields ) );
		assertEquals( "543", record.get( OCSVoiceRecordFields.IMSI ) );

		fields.put( OCSVoiceRawFields.CallingPartyIMSI, "765*#" );
		record.read( makeRawRecord( fields ) );
		assertEquals( "765*#", record.get( OCSVoiceRecordFields.IMSI ) );

		fields.put( OCSVoiceRawFields.CallingPartyIMSI, "*#" );
		record.read( makeRawRecord( fields ) );
		assertEquals( "*#", record.get( OCSVoiceRecordFields.IMSI ) );

		fields.put( OCSVoiceRawFields.CallingPartyIMSI, "455****2.0*#" );
		record.read( makeRawRecord( fields ) );
		assertNull( record.get() );
		assertEquals( "CallingPartyIMSI should conform to pattern ^[0-9*#]*$ : 455****2.0*#",
				record.getFailureReason() );

		fields.put( OCSVoiceRawFields.CallingPartyIMSI, "-455" );
		record.read( makeRawRecord( fields ) );
		assertNull( record.get() );
		assertEquals( "CallingPartyIMSI should conform to pattern ^[0-9*#]*$ : -455",
				record.getFailureReason() );
	}

	@Test
	public void testExtractCurrentBalance() {
		fields = getRequiredInputFields();

		fields.put( OCSVoiceRawFields.PrepaidBalance, "" );
		record.read( makeRawRecord( fields ) );
		fields.put( OCSVoiceRawFields.PrepaidBalance, "abc" );
		record.read( makeRawRecord( fields ) );
		assertNull( record.get() );
		assertEquals( "PrepaidBalance is not a float: abc",
				record.getFailureReason() );
		fields.put( OCSVoiceRawFields.PrepaidBalance, "40000" );
		record.read( makeRawRecord( fields ) );
		assertEquals( "0.0004", record.get( OCSVoiceRecordFields.CURRENT_BALANCE ) );

		fields.put( OCSVoiceRawFields.PrepaidBalance, "59999" );
		record.read( makeRawRecord( fields ) );
		assertEquals( "0.0006", record.get( OCSVoiceRecordFields.CURRENT_BALANCE ) );
	}

	@Test
	public void testExtractCellId() {
		fields = getRequiredInputFields();
		fields.put( OCSVoiceRawFields.CallingCellID, "" );
		record.read( makeRawRecord( fields ) );
		assertEquals( "", record.get( OCSVoiceRecordFields.CELL_ID ) );

		fields.put( OCSVoiceRawFields.CallingCellID, "429020114111313" );
		record.read( makeRawRecord( fields ) );
		assertEquals( "429020114111313", record.get( OCSVoiceRecordFields.CELL_ID ) );
	}

	@Test
	public void testCellIdDetails() {
		fields = getRequiredInputFields();
		final CellIdEntity cellId = new CellIdEntity();
		cellId.setLac_cellid( "0114111313" );
		cellId.setProvince( "SAGARMATHA" );
		cellId.setArea( "UDAYAPUR" );
		cellId.setDistrict( "TRIJUGA" );
		new Expectations() {

			{
				mockCellId.getCellIdDetails( "0114111313" );
				result = cellId;
			}
		};
		fields.put( OCSVoiceRawFields.CallingCellID, "429020114111313" );
		record.read( makeRawRecord( fields ) );
		assertEquals( cellId.getProvince(), record.get( OCSVoiceRecordFields.PROVINCE ) );
		assertEquals( cellId.getArea(), record.get( OCSVoiceRecordFields.AREA ) );
		assertEquals( cellId.getDistrict(), record.get( OCSVoiceRecordFields.DISTRICT ) );
	}

	@Test
	public void testExtractIMEI() {
		fields = getRequiredInputFields();
		fields.put( OCSVoiceRawFields.IMEI, "" );
		record.read( makeRawRecord( fields ) );
		assertEquals( Constants.EMPTY, record.get( OCSVoiceRecordFields.IMEI ) );

		fields.put( OCSVoiceRawFields.IMEI, "45" );
		record.read( makeRawRecord( fields ) );
		assertEquals( "45", record.get( OCSVoiceRecordFields.IMEI ) );

		fields.put( OCSVoiceRawFields.IMEI, "NULL" );
		record.read( makeRawRecord( fields ) );
		assertEquals( Constants.EMPTY, record.get( OCSVoiceRecordFields.IMEI ) );
	}

	@Test
	public void testExtractCallEndReason() {
		fields = getRequiredInputFields();
		fields.put( OCSVoiceRawFields.TerminationReason, "" );
		record.read( makeRawRecord( fields ) );
		assertEquals( Constants.NA, record.get( OCSVoiceRecordFields.CALL_END_REASON ) );

		fields.put( OCSVoiceRawFields.TerminationReason, "2" );
		record.read( makeRawRecord( fields ) );
		assertEquals( "2", record.get( OCSVoiceRecordFields.CALL_END_REASON ) );
	}

	@Test
	public void testExtractANumber() {
		fields = getRequiredInputFields();
		fields.put( OCSVoiceRawFields.CallingPartyNumber, "" );
		record.read( makeRawRecord( fields ) );
		assertNull( record.get( OCSVoiceRecordFields.A_NUMBER ) );

		fields.put( OCSVoiceRawFields.ServiceFlow, Constants.RecordType.OUTGOING );
		fields.put( OCSVoiceRawFields.CallingPartyNumber, "1234567891" );
		record.read( makeRawRecord( fields ) );
		assertEquals( "9771234567891", record.get( OCSVoiceRecordFields.A_NUMBER ) );

		fields.put( OCSVoiceRawFields.ServiceFlow, Constants.RecordType.INCOMING );
		fields.put( OCSVoiceRawFields.CallingPartyNumber, "123456789" );
		record.read( makeRawRecord( fields ) );
		assertEquals( "123456789", record.get( OCSVoiceRecordFields.A_NUMBER ) );

		fields.put( OCSVoiceRawFields.CallingPartyNumber, "455****2.0*#" );
		record.read( makeRawRecord( fields ) );
		assertNull( record.get() );
		assertEquals( "CallingPartyNumber should conform to pattern ^[0-9*#]*$ : 455****2.0*#",
				record.getFailureReason() );

		fields.put( OCSVoiceRawFields.CallingPartyNumber, "-455" );
		record.read( makeRawRecord( fields ) );
		assertNull( record.get() );
		assertEquals( "CallingPartyNumber should conform to pattern ^[0-9*#]*$ : -455",
				record.getFailureReason() );
	}

	@Test
	public void testExtractBNumber() {
		fields = getRequiredInputFields();
		fields.put( OCSVoiceRawFields.CalledPartyNumber, "" );
		record.read( makeRawRecord( fields ) );
		assertTrue( record.isEmpty() );
		assertNull( record.get( OCSVoiceRecordFields.B_NUMBER ) );

		fields.put( OCSVoiceRawFields.ServiceFlow, Constants.RecordType.INCOMING );
		fields.put( OCSVoiceRawFields.CalledPartyNumber, "1234567891" );
		record.read( makeRawRecord( fields ) );
		assertEquals( "9771234567891", record.get( OCSVoiceRecordFields.B_NUMBER ) );

		fields.put( OCSVoiceRawFields.CalledPartyNumber, Constants.RecordType.INCOMING );
		fields.put( OCSVoiceRawFields.CalledPartyNumber, "1234567891" );
		record.read( makeRawRecord( fields ) );
		assertEquals( "9771234567891", record.get( OCSVoiceRecordFields.B_NUMBER ) );
	}

	@Test
	public void testType() {
		fields = getRequiredInputFields();
		fields.put( OCSVoiceRawFields.PayType, "0" );
		record.read( makeRawRecord( fields ) );
		assertEquals( "0", record.get( OCSVoiceRecordFields.TYPE ) );

		fields.put( OCSVoiceRawFields.PayType, "" );
		record.read( makeRawRecord( fields ) );
		assertEquals( "9", record.get( OCSVoiceRecordFields.TYPE ) );

	}

	private RawRecord<OCSVoiceRawFields>
			makeRawRecord( EnumMap<OCSVoiceRawFields, String> fields ) {
		StringBuilder builder = new StringBuilder();
		for ( OCSVoiceRawFields key : OCSVoiceRawFields.values() ) {
			if ( fields.containsKey( key ) )
				builder.append( fields.get( key ) );
			builder.append( "," );
		}

		String rawLine = builder.toString();
		RawRecord<OCSVoiceRawFields> raw =
				new RawRecord<>( OCSVoiceRawFields.class, "," );
		raw.read( rawLine );
		return raw;
	}

	private EnumMap<OCSVoiceRawFields, String> getRequiredInputFields() {
		EnumMap<OCSVoiceRawFields, String> fields =
				new EnumMap<>( OCSVoiceRawFields.class );
		fields.put( OCSVoiceRawFields.CallingPartyNumber, "977123458" );
		fields.put( OCSVoiceRawFields.CalledPartyNumber, "9771234589891" );
		fields.put( OCSVoiceRawFields.ChargingTime, "20150707165959" );
		fields.put( OCSVoiceRawFields.CallDuration, "40" );
		fields.put( OCSVoiceRawFields.ServiceFlow, "1" );
		return fields;
	}

}
