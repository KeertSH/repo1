package com.pelatro.adaptor.ncell.db.dto;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table( name = "ADAPTOR_PROCESS_LOG" )
public class ProcessLogEntry {

	@Id @GeneratedValue( strategy = GenerationType.SEQUENCE,
			generator = "PROCESS_LOG_SEQ" ) @SequenceGenerator(
					name = "PROCESS_LOG_SEQ",
					sequenceName = "PROCESS_LOG_SEQ" ) @Column( name = "ID" ) private long ID;

	@Column( name = "ADAPTOR_TYPE_ID" ) private int adaptorTypeID;
	@Column( name = "FILE_NAME" ) private String fileName;
	@Column( name = "START_TIME" ) private Timestamp startTime;
	@Column( name = "ELAPSED_MS" ) private long elapsedMillis;
	@Column( name = "PARSED" ) private int parsed;
	@Column( name = "REJECTED" ) private int rejected;
	@Column( name = "SKIPPED" ) private int skipped;
	@Column( name = "MISSED" ) private int missing;
	@Column( name = "TALLY" ) private int tally;

	public long getID() {
		return ID;
	}

	public void setID( long ID ) {
		this.ID = ID;
	}

	public int getAdaptorTypeID() {
		return adaptorTypeID;
	}

	public void setAdaptorTypeID( int adaptorTypeID ) {
		this.adaptorTypeID = adaptorTypeID;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName( String fileName ) {
		this.fileName = fileName;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime( Timestamp startTime ) {
		this.startTime = startTime;
	}

	public long getElapsedMillis() {
		return elapsedMillis;
	}

	public void setElapsedMillis( long elapsedMillis ) {
		this.elapsedMillis = elapsedMillis;
	}

	public int getParsed() {
		return parsed;
	}

	public void setParsed( int parsed ) {
		this.parsed = parsed;
	}

	public int getRejected() {
		return rejected;
	}

	public void setRejected( int rejected ) {
		this.rejected = rejected;
	}

	public int getSkipped() {
		return skipped;
	}

	public void setSkipped( int skipped ) {
		this.skipped = skipped;
	}

	public int getMissing() {
		return missing;
	}

	public void setMissing( int missing ) {
		this.missing = missing;
	}

	public int getTally() {
		return tally;
	}

	public void setTally( int tally ) {
		this.tally = tally;
	}
}
