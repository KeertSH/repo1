package com.pelatro.adaptor.ncell;

import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.pelatro.adaptor.common.Adaptor;
import com.pelatro.adaptor.common.StringWrapper;
import com.pelatro.adaptor.ncell.client.ToolsFactory;
import com.pelatro.adaptor.ncell.common.AdaptorType;
import com.pelatro.adaptor.ncell.common.Constants;
import com.pelatro.adaptor.ncell.common.PropertyUtil;
import com.pelatro.adaptor.ncell.common.Settings;
import com.pelatro.adaptor.ncell.ocs.voice.OCSVoiceAdaptor;
import com.pelatro.adaptor.streams.SentenceReader;
import com.pelatro.adaptor.streams.Stream;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class AdaptorRunner {
	public static Logger logger=LogManager.getLogger(AdaptorRunner.class);
	private static PropertyUtil propertyUtil = PropertyUtil.getInstance();
	public static final long millis=10000;
	public static final DateTimeFormatter contextDateFormat =
			DateTimeFormat.forPattern( "CCYYMMdd" );
	
	public Namespace parseArgument( String[] args ) throws ArgumentParserException {
		ArgumentParser parser = ArgumentParsers.newFor( AdaptorRunner.class.getName() ).build()
				.defaultHelp( true )
				.description( propertyUtil.getProperty( "message.adaptortype" ) );

		Set<AdaptorType> adaptorTypes = EnumSet.allOf( AdaptorType.class );

		Set<String> adaptorNames = new HashSet<>();
		for ( AdaptorType adaptorType : adaptorTypes )
			adaptorNames.add( adaptorType.label );

		parser.addArgument( "-t", "--type" ).choices( adaptorNames.toArray() ).required( true )
				.help( propertyUtil.getProperty( "message.adaptorname" ) );

		parser.addArgument( "-c", "--config" ).required( true )
				.help( propertyUtil.getProperty( "message.adaptorconfig" ) );

		parser.addArgument( "-b", "--cabinet-config" ).required( false )
				.help( "Configuration file to connect to the cabinet" );

		parser.addArgument( "--useCache" )
				.help( propertyUtil.getProperty( "message.subscribercache" ) )
				.setDefault( false ).action( Arguments.storeTrue() );

		parser.addArgument( "--daemon" ).help( propertyUtil.getProperty( "message.daemon" ) )
				.action( Arguments.storeTrue() );

		parser.addArgument( "--source-label" )
				.help( propertyUtil.getProperty( "message.sourcelabel" ) )
				.setDefault( "file" );

		parser.addArgument( "--skip-db-logging" )
				.help( propertyUtil.getProperty( "message.skipdblogging" ) )
				.action( Arguments.storeTrue() );
		
		parser.addArgument( "--containsHeader" ).help( "message.Header" )
				.action( Arguments.storeTrue() );
		Namespace namespace = null;
		try {
			namespace = parser.parseArgs( args );
		}
		catch ( ArgumentParserException ape ) {
			parser.handleError( ape );
			throw ( ape );
		}

		logger.info( "Adaptor is starting with options:" + namespace.getAttrs().toString() );
		return namespace;
	}
	
	public Stream parseSentance( String label, String configPathName ) {
		String raw = new SentenceReader( configPathName ).getParagraph( label );
		if ( raw == null || raw.isEmpty() ) {
			throw new IllegalArgumentException( String.format(
					"Configuration file %s does not contain %s", configPathName, label ) );
		}

		logger.info( String.format( "Configuring STREAM using %s ...", label ) );
		try {
			for ( String line : StringWrapper.greedily( raw, 75, .3 ) )
				LogManager.getLogger( "vanillaLogger" )
						.info( String.format( "%2s%s", "", line ));
		}
		catch ( Exception e ) {
			logger.error( e );
		}

		return Stream.form( raw );
	}
	public void run( String type, Stream stream, boolean isDaemon, String SourceLabel,
			boolean skipDbLogging ) {
			AdaptorType adaptorType = AdaptorType.decode( type );
			Settings.setFileNameLabel( String.format( "%%%s%%", SourceLabel ) );
			Settings.setSummaryLogged2db( !skipDbLogging );

			if ( adaptorType == null ) {
				logger.error( "Unknown adaptor type: %s", type );
				throw new IllegalArgumentException(
					String.format( "Unknown adaptor type: %s", type ) );
			}

			try {
				Adaptor adaptor = getAdaptor( adaptorType );
				do {
					adaptor.process( stream );
					Thread.sleep( millis );
				}
				while ( isDaemon );

			}
			catch ( InterruptedException ie ) {
				logger.error( "Error in Running adaptor %s", ie.getMessage() );
			}
		}

		public void close( Stream stream ) {
			stream.close();
		}

		private Adaptor getAdaptor( AdaptorType adaptorType ) {
			Adaptor adaptor = new OCSVoiceAdaptor(adaptorType);
			return adaptor;
		}
		public static void main( String[] args ) {
			AdaptorRunner runner = null;
			Stream stream = null;
			try {
				runner = new AdaptorRunner();
				Namespace namespace = runner.parseArgument( args );
				Constants.useCache = namespace.getBoolean( "useCache" );
				Constants.containsHeader = namespace.getBoolean( "containsHeader" );
				stream = runner.parseSentance( namespace.getString( "type" ),
						namespace.getString( "config" ) );
				stream.setSticky( new AdaptorSticky() );
				//AdaptorRunner.setupFactory( namespace.getString( "cabinet_config" ) );
				runner.run( namespace.getString( "type" ), stream,
						namespace.getBoolean( "daemon" ), namespace.getString( "source_label" ),
						namespace.getBoolean( "skip_db_logging" ) );

			}
			catch ( ArgumentParserException ape ) {
				System.exit( 1 );
			}
			catch ( Throwable e ) {
				logger.fatal( e.getMessage(), e );
				System.exit( 1 );
			}
			finally {
				logger.info( "Stopping Adaptor...." );
				if ( runner != null )
					runner.close( stream );
			}
			System.exit( 0 );
		}

		static void setupFactory( String clientConfigPath ) {
			if ( clientConfigPath == null )
				return;

			Path jsonPath = Paths.get( clientConfigPath );
			try (Reader reader = Files.newBufferedReader( jsonPath, Charset.forName( "UTF-8" ) )) {
				JSONParser parser = new JSONParser();
				JSONObject configuration = ( JSONObject ) parser.parse( reader );
				ToolsFactory.initialize( configuration );
			}
			catch ( Exception e ) {
				logger.error( "Error while reading client configuration", e );
				throw new IllegalArgumentException( e );
			}

		}

}
