package com.pelatro.adaptor.ncell.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import com.pelatro.adaptor.ncell.common.PropertyUtil;

public class DbHenchman {

	private static DbHenchman _henchman = null;
	private static Logger logger = LogManager.getLogger( DbHenchman.class );
	private static PropertyUtil propertyUtil = PropertyUtil.getInstance();
	private SessionFactory sessionFactory = null;
	private StatelessSession staleSession;

	private DbHenchman() {
		logger.info( "DBHenchman getting started.." );
		sessionFactory = _createSessionFactory();
		createSession();
	}

	private void createSession() {
		staleSession = sessionFactory.openStatelessSession();
	}

	public StatelessSession getSession() {
		return staleSession;
	}

	public static DbHenchman getHenchman() {
		if ( _henchman == null )
			_henchman = new DbHenchman();
		return _henchman;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	private SessionFactory _createSessionFactory() {
		logger.info( "Creating Session Factory" );
		try {
			Configuration configuration = new Configuration();
			configuration.configure( propertyUtil.getProperty( "hibernate.config.file" ) );
			configuration
					.configure( propertyUtil.getProperty( "ncell.hibernate.config.file" ) );
			ServiceRegistry serviceRegistry =
					new StandardServiceRegistryBuilder()
							.applySettings( configuration.getProperties() ).build();
			logger.info( "Created Sesssion Factory Successfully" );
			return configuration.buildSessionFactory( serviceRegistry );
		}
		catch ( Throwable t ) {
			t.printStackTrace();
			logger.error( String.format( "Initial SessionFactory creation failed. %s",
					t.toString() ) );
			throw new ExceptionInInitializerError( t );
		}
	}

	public void close() {
		staleSession.close();
	}
}
