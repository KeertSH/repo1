package com.pelatro.adaptor.ncell.client;

import com.pelatro.adaptor.streams.Context;
import com.pelatro.adaptor.streams.EventType;

public interface ServiceCallback {

	public void onResult( Context context );

	public void onEvent( EventType eventType, Context context );

	public FileTracker getFileTracker();
}
