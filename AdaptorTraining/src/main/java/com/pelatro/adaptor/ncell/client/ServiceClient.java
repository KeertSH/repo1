package com.pelatro.adaptor.ncell.client;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pelatro.cabinet.ncell.protocol.CabinetProtocol;
import com.pelatro.cabinet.ncell.protocol.CabinetProtocol.CabinetRequest;
import com.pelatro.cabinet.ncell.protocol.CabinetProtocol.CabinetResponse;
import com.pelatro.cabinet.ncell.protocol.CabinetProtocol.ErrorResponse;
import com.pelatro.cabinet.ncell.protocol.CabinetProtocol.SearchResponse;
import com.pelatro.cabinet.ncell.protocol.CabinetProtocol.CabinetResponse.ResponseType;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

public class ServiceClient extends SimpleChannelInboundHandler<CabinetResponse> {

	private static final Logger logger = LogManager.getLogger( ServiceClient.class );

	private EventLoopGroup group;
	private Channel channel;
	private Bootstrap bootstrap;

	private final ResponseHook responseHook;
	private final CancelHook cancelHook;

	private final InetSocketAddress address;

	public ServiceClient( InetSocketAddress address,
			ResponseHook responseHook, CancelHook cancelHook ) {

		this.responseHook = responseHook;
		this.cancelHook = cancelHook;

		this.address = address;

		group = new NioEventLoopGroup();

		try {
			logger.info( "Bootstrapping ..." );
			bootstrap = new Bootstrap();
			bootstrap
					.group( group )
					.channel( NioSocketChannel.class )
					.handler( getChannelInitializer() );

			logger.info( "... done" );
		}
		catch ( Exception e ) {
			logger.error( "Unable to initialise service handler", e );
			throw new RuntimeException( e );
		}
	}

	private void connectIfRequired() {
		if ( channel != null && channel.isOpen() )
			return;

		logger.info( String.format( "connecting (to %s) ...", address ) );

		ChannelFuture future = bootstrap.connect( address );
		try {
			future.await( 10, TimeUnit.SECONDS );
		}
		catch ( InterruptedException e ) {
			channel = null;
			return;
		}

		if ( future.isSuccess() ) {
			logger.info( String.format( "... connected (to %s)", address ) );
			channel = future.channel();
			return;
		}

		logger.error( String.format( "... connection failed (to %s)", address ) );
		channel = null;
	}

	public boolean postRequest( CabinetRequest req ) {
		connectIfRequired();
		if ( channel == null )
			return false;

		channel.writeAndFlush( req );
		return true;
	}

	@Override
	protected void channelRead0( ChannelHandlerContext ctx, CabinetResponse message )
			throws Exception {

		if ( message.getType().equals( ResponseType.ErrorResponse ) ) {
			ErrorResponse error = message.getErrorResponse();
			logger.warn( String.format(
					"Error %s: %s ", error.getCode(), error.getMessage() ) );

			cancelHook.onCancel( message.getRequestId() );
			return;
		}

		if ( message.getType().equals( ResponseType.SearchResponse ) ) {
			SearchResponse response = message.getSearchResponse();
			ByteBuffer values = response.getValues().asReadOnlyByteBuffer();
			responseHook.onResponse( message.getRequestId(), values );
			return;
		}

		logger.warn( String.format( "Unexpected message %s ", message.getType().name() ) );
	}

	@Override
	public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause ) {
		logger.error( "Exception in cabinet handler. Swallowing", cause );
	}

	private ChannelInitializer<SocketChannel> getChannelInitializer() {

		final ServiceClient me = this;
		return new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel( SocketChannel ch ) throws Exception {
				ChannelPipeline p = ch.pipeline();

				p.addLast( new ProtobufVarint32FrameDecoder() );
				p.addLast( new ProtobufDecoder(
					CabinetProtocol.CabinetResponse.getDefaultInstance() ) );

				p.addLast( new ProtobufVarint32LengthFieldPrepender() );
				p.addLast( new ProtobufEncoder() );

				p.addLast( me );
			}
		};
	}

	public void close() {
		channel.close();
		group.shutdownGracefully();
	}
}
