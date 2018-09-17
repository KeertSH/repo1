package com.pelatro.adaptor.ncell.client;

import java.nio.ByteBuffer;

public interface ResponseHook {

	public void onResponse(String requestId, ByteBuffer responseBody );

}
