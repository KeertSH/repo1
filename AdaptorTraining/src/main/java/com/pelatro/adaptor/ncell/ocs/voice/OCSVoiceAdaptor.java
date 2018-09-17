package com.pelatro.adaptor.ncell.ocs.voice;

import static com.pelatro.adaptor.ncell.common.Labels.CTX_FILTER;
import static com.pelatro.adaptor.ncell.common.Labels.CTX_RAW;
import static com.pelatro.adaptor.ncell.common.Labels.CTX_REASON;
import static com.pelatro.adaptor.ncell.common.Labels.CTX_RECORD;
import static com.pelatro.adaptor.ncell.common.Labels.CTX_SOURCE;
import static com.pelatro.adaptor.ncell.common.Labels.CTX_SUMMARY;
import static com.pelatro.adaptor.ncell.common.Labels.CTX_FILE;
import java.io.BufferedReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pelatro.adaptor.common.RawRecord;
import com.pelatro.adaptor.ncell.common.AdaptorType;
import com.pelatro.adaptor.ncell.common.CDRRecordStatus;
import com.pelatro.adaptor.ncell.common.Constants;
import com.pelatro.adaptor.ncell.common.NcellAdaptor;
import com.pelatro.adaptor.ncell.common.ParseSummaryWithDBLogging;
import com.pelatro.adaptor.ncell.db.AccountDetailsLookup;
import com.pelatro.adaptor.ncell.db.CellIdDetailsLookup;
import com.pelatro.adaptor.ncell.db.NumberPrefixLookup;
import com.pelatro.adaptor.ncell.db.OffnetNumberPrefixLookup;
import com.pelatro.adaptor.ncell.db.PeriodLookup;
import com.pelatro.adaptor.ncell.db.ShortCodeLookup;
import com.pelatro.adaptor.streams.EventType;
import com.pelatro.adaptor.streams.Stream;

public class OCSVoiceAdaptor extends NcellAdaptor {

	private NumberPrefixLookup callTypeLookup;
	private CellIdDetailsLookup cellIdLookup;
	private OffnetNumberPrefixLookup offNetLookup;
	private ShortCodeLookup shortCodeLookup;
	public final 	ParseSummaryWithDBLogging summary;
	private static Logger logger=LogManager.getLogger(OCSVoiceAdaptor.class);
	public OCSVoiceAdaptor(AdaptorType adaptorType) {
		super(adaptorType);
		logger.info( "Initializing Prepaid OCS Voice Adaptor" );
		PeriodLookup.initialize();
		AccountDetailsLookup.initialize();
		this.callTypeLookup = new NumberPrefixLookup();
		this.cellIdLookup = new CellIdDetailsLookup();
		this.offNetLookup = new OffnetNumberPrefixLookup();
		this.shortCodeLookup = new ShortCodeLookup();
		this.summary = new ParseSummaryWithDBLogging( AdaptorType.OCS_VOICE, txManager );
	}
	@Override
	protected void processFile(Stream stream) throws Exception {
		BufferedReader reader = stream.context.getReader( CTX_SOURCE );
		if ( reader == null ) {
			logger.error( String.format(
					"STREAM specification missing required label %s. (This is a raw source)",
					CTX_SOURCE ) );
			throw new RuntimeException( "Bad STREAM configuration" );
		}
		RawRecord<OCSVoiceRawFields> rawRecord = new RawRecord<OCSVoiceRawFields>(
			OCSVoiceRawFields.class, "\\|", RawRecord.CheckStyle.RELAXED );
		OCSVoiceRecord prepaidVoiceRecord =
				new OCSVoiceRecord( callTypeLookup, cellIdLookup, offNetLookup,shortCodeLookup );
		summary.start();
		String rawLine = null;
		while ( ( rawLine = reader.readLine() ) != null ) {
			if ( rawLine.isEmpty() )
				continue;
			stream.context.set( CTX_RAW, rawLine );
			stream.context.set( CTX_REASON, ( String ) null );
			stream.context.set( CTX_FILTER, ( String ) null );
			rawRecord.read( rawLine );
			prepaidVoiceRecord.read( rawRecord );
			if ( prepaidVoiceRecord.isEmpty() ) {
				String failureReason = prepaidVoiceRecord.getFailureReason();
				if ( failureReason
						.startsWith( "Could not find a subscriber with" ) ) {
					summary.tally( CDRRecordStatus.MISSING );
					stream.context.set( CTX_FILTER, rawLine );
				}
				else {
					summary.tally( CDRRecordStatus.REJECTED );
					stream.context.set( CTX_FILTER, failureReason );
				}

				String reason = String.format( "Record %6d. %s", summary.getTally(),
						prepaidVoiceRecord.getFailureReason() );
				stream.context.set( CTX_REASON, reason );

				stream.execute( EventType.ON_REJECT );
				continue;
			}
			String type = rawRecord.get( OCSVoiceRawFields.PayType );
			if ( !type
					.equals( Constants.subsciberType.Prepaid ) && !type.equals( null )
					&& !type.isEmpty() && !type.equalsIgnoreCase( "NULL" ) ) {
				summary.tally( CDRRecordStatus.REJECTED );
				stream.context.set( CTX_FILTER, rawLine );

				String reason = String.format( "Record %6d. %s", summary.getTally(),
						"Not a Prepaid Record" );
				stream.context.set( CTX_REASON, reason );

				stream.execute( EventType.ON_REJECT );
				continue;
			}
			if(prepaidVoiceRecord.get(OCSVoiceRecordFields.SUBSCRIBER_ID).equals("0"))
			{
				summary.tally( CDRRecordStatus.MISSING );
				stream.context.set(CTX_RECORD, prepaidVoiceRecord.get());
				stream.context.set( CTX_FILTER, rawLine );
				stream.context.set(CTX_REASON, "Incomplete Database");
				stream.execute(EventType.ON_REJECT);
				continue;
			}
			stream.context.set( CTX_RECORD, prepaidVoiceRecord.get() );
			stream.execute( EventType.ON_PARSE );
			summary.tally( CDRRecordStatus.PARSED );
		}
		summary.end();
		logger.info( stream.context.getString( CTX_FILE ) + ":" + summary.toShortString() );
		stream.context.set( CTX_SUMMARY, summary.toString() );
		stream.execute( EventType.ON_COMPLETE );
	}		
}
