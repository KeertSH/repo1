package com.pelatro.adaptor.ncell;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.pelatro.adaptor.ncell.common.AdaptorType;
import com.pelatro.adaptor.streams.SentenceReader;
import com.pelatro.adaptor.streams.Stream;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

@RunWith( JMockit.class )
public class AdaptorRunnerTest {

	@Tested AdaptorRunner adatporRunner;

	@Mocked ArgumentParser mockedParser;

	@Mocked Namespace mockNamespace;

	@Test
	public void parseArgument( @Mocked final ArgumentParsers mockedArgumentParsers )
			throws Exception {
		final String[] args = { "adaptor", "-t", "voice" };

		new Expectations() {

			{
				ArgumentParsers.newFor( AdaptorRunner.class.getName() ).build()
						.defaultHelp( true ).description( anyString );
				minTimes = 0;
				result = mockedParser;

				mockedParser.parseArgs( args );
				minTimes = 0;
				result = mockNamespace;

			}
		};

		assertNotNull( adatporRunner.parseArgument( args ));
		new Verifications() {

			{

				mockedParser.addArgument( "-t", "--type" ).choices(
						AdaptorType.SUBSCRIBER.label,
						AdaptorType.DEVICE.label,
						AdaptorType.CRM.label,
						AdaptorType.OCS_DATA.label,
						AdaptorType.OCS_COM.label,
						AdaptorType.OCS_SMS.label,
						AdaptorType.OCS_MGR.label,
						AdaptorType.OCS_VOICE.label,
						AdaptorType.OCS_MON.label,
						AdaptorType.OCS_RECHARGE.label).required( true )
						.help( anyString );
				mockedParser.addArgument( "-c", "--config" ).required( true )
						.help( anyString );
				mockedParser.addArgument( "--useCache" ).help( anyString )
						.action( Arguments.storeTrue() );
				mockedParser.addArgument( "--daemon" ).help( anyString )
						.action( Arguments.storeTrue() );
				mockedParser.addArgument( "--skip-db-logging" ).help( anyString )
						.action( Arguments.storeTrue() );
				mockedParser.addArgument( "--containsHeader" ).help( anyString )
						.action( Arguments.storeTrue() );

				mockedParser.addArgument( "--source-label" ).help( anyString )
						.setDefault( "file" );

				mockedParser.parseArgs( args );

			}
		};
	}

	@Test
	public void testParseSentance( @Mocked final Stream mockStream ) {
		final String configPath = "some file path";
		final String label = "type";
		new MockUp<SentenceReader>() {

			@Mock
			public void $init( String configPath ) {
			}

			@Mock
			public String getParagraph( String label ) {
				return "test raw";
			}
		};

		new Expectations() {

			{
				new SentenceReader( configPath ).getParagraph( label );
			}
		};

		adatporRunner.parseSentance( configPath, label );

		new Verifications() {

			{
				Stream.form( "test raw" );
			}
		};
	}

	@Test
	public void testRun() {
		//TODO: Test case needs to be modified after actual implemenation.
	}
}
