package com.pelatro.adaptor.ncell.client;

import static com.pelatro.adaptor.ncell.common.Labels.CTX_DUMMY;
import static com.pelatro.adaptor.ncell.common.Labels.CTX_EVENT;
import static com.pelatro.adaptor.ncell.common.Labels.CTX_FILE_COUNTER;
import static com.pelatro.adaptor.ncell.common.Labels.CTX_LK_MSISDN;
import static com.pelatro.adaptor.ncell.common.Labels.CTX_LV_SUBID;
import static com.pelatro.adaptor.ncell.common.Labels.CTX_LV_UID;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;

import com.google.protobuf.ByteString;
import com.pelatro.adaptor.ncell.db.UniqueIDGenerator;
import com.pelatro.adaptor.streams.Context;
import com.pelatro.adaptor.streams.EventType;
import com.pelatro.adaptor.streams.Publisher;
import com.pelatro.cabinet.ncell.protocol.CabinetProtocol.CabinetRequest;
import com.pelatro.cabinet.ncell.protocol.CabinetProtocol.CabinetRequest.RequestType;
import com.pelatro.cabinet.ncell.protocol.CabinetProtocol.SearchRequest;

public class SubscriberAccountKeyLookup implements Publisher, ResponseHook, CancelHook {

	private static final Logger logger = LogManager.getLogger( SubscriberAccountKeyLookup.class );

	private static final String bindValueSubscriberId = "subscriberId";
	private static final String bindValueUId = "uId";

	public static final String bindParamSubscriberId =
			formBindVariable( bindValueSubscriberId );
	public static final String bindParamUId = formBindVariable( bindValueUId );
	private static final String malformedMsisdn = "0000000000";

	private final Object responseLock = new Object();
	private final Object requestLock = new Object();
	private Queue<List<Context>> outbox;
	private int counter = 0;

	public ServiceCallback callback;
	private ServiceClient client;

	private static String formBindVariable( String raw ) {
		return String.format( "${%s}", raw );
	}

	public SubscriberAccountKeyLookup( ServiceCallback callback ) {
		super();
		this.callback = callback;
		client = ToolsFactory.makeClient( this, this );
		outbox = ToolsFactory.makeOutbox();
	}

	@Override
	public void publish( Iterable<Context> volume ) {
		synchronized (requestLock) {
			StringBuffer body = new StringBuffer();
			List<Context> contexts = new ArrayList<>();
			for ( Context context : volume ) {
				String msisdn = context.getString( CTX_LK_MSISDN );
				msisdn = msisdn.length() == 10 ? msisdn : malformedMsisdn;

				body = body.append( msisdn );
				contexts.add( context );
			}

			while ( !outbox.offer( contexts ) ) {
				logger.debug( "Reached max pending batches, slowing down : " + outbox.size() );
				try {
					Thread.sleep( 100 );
				}
				catch ( InterruptedException e ) {
					logger.error( e );
				}
			}

			counter++;
			postRequest( counter, body.toString().getBytes() );
		}
	}

	public void postRequest( int reqId, byte[] bytes ) {
		logger.trace(
				String.format( "Sending request %d with %d bytes", reqId, bytes.length ) );

		CabinetRequest.Builder msgBuilder = CabinetRequest.newBuilder();

		SearchRequest.Builder builder = SearchRequest.newBuilder();
		builder.setSearch( "account_key" );
		builder.setFill( "id" );
		ByteString keys = ByteString.copyFrom( bytes );
		builder.setKeys( keys );

		msgBuilder.setSearchRequest( builder );
		msgBuilder.setRequestId( String.valueOf( reqId ) );
		msgBuilder.setType( RequestType.SearchRequest );

		CabinetRequest request = msgBuilder.build();

		int times = 0;
		while ( ++times <= 3 ) {
			if ( client.postRequest( request ) )
				return;

			logger.warn( String.format( "Request failed. Retrying (%d / 3)...", times ) );
		}

		logger.error( "Request failed" );
	}

	@Override
	public void onCancel( String requestId ) {
		int size = 0;
		Iterable<Context> volume = outbox.poll();
		for ( Context context : volume ) {
			callback.onResult( context );
			handleEvents( context );
			++size;
		}

		logger.warn( String.format( "Cancelled a request of %d records", size ) );
	}

	private void handleEvents( Context context ) {
		if ( context.get( CTX_EVENT ) != null ) {
			EventType eventType = context.get( CTX_EVENT );
			callback.onEvent( eventType, context );
		}
	}

	@Override
	public void onResponse( String requestId, ByteBuffer values ) {
		synchronized (responseLock) {
			FileTracker closedFilesTracker = callback.getFileTracker();
			List<Context> contexts = outbox.poll();
			int position = 0;
			int skipped = 0;
			int lookupFailures = 0;
			while ( values.hasRemaining() ) {
				int id = values.getInt();

				Context context = contexts.get( position++ );
				Long fileCounter = context.get( CTX_FILE_COUNTER );

				if ( fileCounter != null &&
						fileCounter == closedFilesTracker.popTill( fileCounter ) ) {
					++skipped;
					continue;
				}

				Boolean isDummy = context.get( CTX_DUMMY );
				if ( isDummy != null && isDummy ) {
					handleEvents( context );
					continue;
				}

				if ( id > 0 ) {
					context.set( CTX_LV_SUBID, String.valueOf( id ) );
					context.set( CTX_LV_UID, String.valueOf( UniqueIDGenerator.generate() ) );
				}
				else
					++lookupFailures;

				callback.onResult( context );
				handleEvents( context );
			}

			if ( skipped > 0 )
				logger.info( String.format(
						"Discarded %d response(s) as the file was already closed", skipped ) );

			if ( lookupFailures > 0 )
				logger.debug( String.format( "%d lookup failures", lookupFailures ) );
		}
	}

	public static String stamp( String record, String subscriberId, String uuid ) {
		Map<String, String> values = new HashMap<>();
		values.put( bindValueSubscriberId, subscriberId );
		values.put( bindValueUId, uuid );
		StrSubstitutor sub = new StrSubstitutor( values );
		return sub.replace( record );
	}
}
