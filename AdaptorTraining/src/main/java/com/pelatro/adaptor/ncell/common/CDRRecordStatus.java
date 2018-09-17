package com.pelatro.adaptor.ncell.common;

public enum CDRRecordStatus {
	PARSED( "Parsed" ),
	REJECTED( "Rejected" ),
	SKIPPED( "Skipped" ),
	MISSING( "Invalid Subscriber" );

	private final String readable;

	private CDRRecordStatus( String readable ) {
		this.readable = readable;
	}

	public String toString() {
		return readable;
	}
}
