package com.pelatro.adaptor.ncell.common;

import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PropertyUtil {

	private static PropertyUtil _instance = null;

	private Properties properties = null;
	private static final Logger logger = LogManager.getLogger( PropertyUtil.class );

	private PropertyUtil() {
		try {
			properties = new Properties();
			properties.load( getClass().getClassLoader()
					.getResourceAsStream( "ncell-adaptor.properties" ) );
		}
		catch ( IOException ioe ) {
			System.out.println( "Not able to load the properites" );
			logger.error( "Failed to load configuration file:" + ioe );
		}
	}

	public static PropertyUtil getInstance() {
		if ( _instance == null )
			_instance = new PropertyUtil();
		return _instance;
	}

	public String getProperty( String property ) {
		return properties.getProperty( property );
	}
}
