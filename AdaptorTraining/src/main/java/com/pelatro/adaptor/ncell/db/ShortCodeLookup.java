package com.pelatro.adaptor.ncell.db;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.StatelessSession;

import com.pelatro.adaptor.ncell.db.dto.ShortCodeEntity;

public class ShortCodeLookup {

	private static Logger logger = LogManager.getLogger( ShortCodeLookup.class );

	private Query query;
	private StatelessSession session = null;
	private List<String> shortCodeDetails = new ArrayList<>();;

	@SuppressWarnings( { "unchecked" } )
	public ShortCodeLookup() {
		logger.info( "Constructing ShortCodeDetailsDB." );
		session =
				DbHenchman.getHenchman().getSessionFactory().openStatelessSession();
		query = session.createQuery( "FROM ShortCodeEntity" );
		List<ShortCodeEntity> shortCodeDetailsList = query.list();

		shortCodeDetails.clear();
		for ( ShortCodeEntity shortCode : shortCodeDetailsList )
			shortCodeDetails.add( shortCode.getShort_code_value() );

		session.close();
	}

	public boolean isShortCode( String bnumber ) {
		Boolean isShortIde = false;
		isShortIde = shortCodeDetails.contains( bnumber.trim() );
		return isShortIde;
	}

}
