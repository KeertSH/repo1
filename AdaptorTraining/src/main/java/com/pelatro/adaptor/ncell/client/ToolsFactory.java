package com.pelatro.adaptor.ncell.client;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.simple.JSONObject;

import com.pelatro.adaptor.ncell.common.JSONUtil;
import com.pelatro.adaptor.ncell.common.ParseSummaryWithDBLogging;
import com.pelatro.adaptor.streams.Annals;
import com.pelatro.adaptor.streams.Context;
import com.pelatro.adaptor.streams.Publisher;

public class ToolsFactory {

	private static final String tagClient = "client";
	private static final String tagHost = "host";
	private static final String tagPort = "port";
	private static final String tagBatchSize = "batch-size";
	private static final String tagBatchTimeout = "batch-timeout";
	private static final String tagMaxPendingBatches = "max-pending-batches";
	private static final String tagMaxOpenFiles = "max-open-files";

	private static String host;
	private static Long port;
	private static Long batchSize;
	private static Long batchTimeout;
	private static Long maxPendingBatches;
	private static Long maxOpenFiles;
	private static boolean initialized = false;

	public static void initialize( JSONObject fullConfig ) {
		JSONObject configuration = JSONUtil.parse( fullConfig, tagClient );
		host = JSONUtil.parse( configuration, tagHost, "127.0.0.1" );
		port = JSONUtil.parse( configuration, tagPort, 8463l );
		batchSize = JSONUtil.parse( configuration, tagBatchSize, 1000l );
		batchTimeout = JSONUtil.parse( configuration, tagBatchTimeout, -1l );
		maxPendingBatches = JSONUtil.parse( configuration, tagMaxPendingBatches, 10l );
		maxOpenFiles = JSONUtil.parse( configuration, tagMaxOpenFiles, 10l );
		initialized = true;
	}

	public static Annals makeStore( Publisher lookup ) {
		if ( !initialized )
			throw new IllegalArgumentException( "Factory is not initialized for make" );

		return new Annals( lookup, batchSize.intValue(), batchTimeout );
	}

	public static Queue<ParseSummaryWithDBLogging> makeSummaryQueue() {
		if ( !initialized )
			throw new IllegalArgumentException( "Factory is not initialized for make" );

		return new LinkedBlockingQueue<ParseSummaryWithDBLogging>( maxOpenFiles.intValue() );
	}

	public static ServiceClient makeClient(
		ResponseHook responseHook, CancelHook cancelHook ) {

		if ( !initialized )
			throw new IllegalArgumentException( "Factory is not initialized for make" );

		InetSocketAddress address = new InetSocketAddress( host, port.intValue() );
		return new ServiceClient( address, responseHook, cancelHook );
	}

	public static Queue<List<Context>> makeOutbox() {
		if ( !initialized )
			throw new IllegalArgumentException( "Factory is not initialized for make" );

		return new LinkedBlockingQueue<List<Context>>( maxPendingBatches.intValue() );
	}

}