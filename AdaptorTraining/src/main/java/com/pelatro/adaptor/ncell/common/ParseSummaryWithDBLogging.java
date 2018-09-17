package com.pelatro.adaptor.ncell.common;

import java.sql.Timestamp;

import org.joda.time.Duration;

import com.pelatro.adaptor.common.ParseSummary;
import com.pelatro.adaptor.ncell.db.dto.ProcessLogEntry;

public class ParseSummaryWithDBLogging extends ParseSummary<CDRRecordStatus> {

	private final AdaptorType adaptorType;
	private final TransactionManager txManager;

	public ParseSummaryWithDBLogging( AdaptorType adaptorType,
			TransactionManager txManager ) {
		super( CDRRecordStatus.class );
		this.adaptorType = adaptorType;
		this.txManager = txManager;
	}

	public void log2db( String fileName ) {
		ProcessLogEntry entry = new ProcessLogEntry();

		entry.setAdaptorTypeID( adaptorType.id );
		entry.setStartTime( new Timestamp( startTime.getMillis() ) );
		entry.setFileName( fileName );
		entry.setElapsedMillis( new Duration( startTime, endTime ).getMillis() );
		entry.setParsed( getTally( CDRRecordStatus.PARSED ) );
		entry.setRejected( getTally( CDRRecordStatus.REJECTED ) );
		entry.setSkipped( getTally( CDRRecordStatus.SKIPPED ) );
		entry.setMissing( getTally( CDRRecordStatus.MISSING ) );
		entry.setTally( getTally() );
		txManager.session.save( entry );
		txManager.increment();
	}
}
