package com.pelatro.adaptor.ncell.client;

import java.util.concurrent.ArrayBlockingQueue;

public class FileTracker {

	private long counter;
	private final ArrayBlockingQueue<Long> trackedFiles;

	public FileTracker() {
		trackedFiles = new ArrayBlockingQueue<>( 1000 );
	}

	public void reset() {
		trackedFiles.clear();
		counter = 0;
	}

	public long getCounter() {
		return counter;
	}

	public void bumpCounter() {
		++counter;
	}

	public boolean offer( long l ) {
		if ( !trackedFiles.contains( l ) )
			return trackedFiles.offer( l );

		return true;
	}

	public long popTill( long l ) {
		while ( peek() < l )
			trackedFiles.poll();

		return peek();
	}

	public long peek() {
		Long top = trackedFiles.peek();
		return top == null ? Long.MAX_VALUE : top;
	}

	public int capacity() {
		return trackedFiles.remainingCapacity();
	}
}
