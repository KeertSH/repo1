package com.pelatro.adaptor.ncell.db;

import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.StatelessSession;

import com.pelatro.adaptor.ncell.common.Constants;
import com.pelatro.adaptor.ncell.db.dto.OffNetNumberPrefixEntity;

public class OffnetNumberPrefixLookup {

	private static final HashMap<String, OffNetNumberPrefixEntity> networkCodeDetails =
			new HashMap<>();
	private static Logger logger = LogManager.getLogger( OffnetNumberPrefixLookup.class );

	private Query query;
	private StatelessSession session = null;
	private List<OffNetNumberPrefixEntity> offnetCodeList;

	@SuppressWarnings( "unchecked" )
	public OffnetNumberPrefixLookup() {

		session = DbHenchman.getHenchman().getSessionFactory().openStatelessSession();

		logger.info( "Loading NetworkCode and Operator Details..." );

		query = session.createQuery( " from OffNetNumberPrefixEntity " );
		offnetCodeList = query.list();

		logger.info(
				String.format( "... %d NetworkCode slurped ...", offnetCodeList.size() ) );

		for ( int i = 0; i < offnetCodeList.size(); i++ ) {
			networkCodeDetails.put( offnetCodeList.get( i ).getPrefix(),
					offnetCodeList.get( i ) );
		}

		logger.info(
				String.format( "... %d NetworkCode registered..", offnetCodeList.size() ) );

		session.close();
	}

	public String operatorIdlookup( String bnumber ) {
		if ( bnumber.startsWith( Constants.NCELL_COUNTRY_CODE ) ) {
			bnumber = bnumber.substring( 3, bnumber.length() );
			int i = bnumber.length();
			while ( i > 0 ) {
				bnumber = bnumber.substring( 0, i );
				if ( networkCodeDetails.containsKey( bnumber ) ) {
					String operatorid = String
							.valueOf( networkCodeDetails.get( bnumber ).getOperator_id() );
					return operatorid;
				}
				i--;
				continue;
			}
		}
		return "9";
	}

	public String callTypelookup( String bnumber ) {
		if ( bnumber.startsWith( Constants.NCELL_COUNTRY_CODE ) ) {
			bnumber = bnumber.substring( 3, bnumber.length() );
			if ( networkCodeDetails.containsKey( bnumber ) ) {
				String calltype =
						String.valueOf( networkCodeDetails.get( bnumber ).getCall_type() );
				return calltype;
			}
			else {
				int i = bnumber.length();
				while ( i > 0 ) {
					bnumber = bnumber.substring( 0, i );
					if ( networkCodeDetails.containsKey( bnumber ) ) {
						String calltype = String
								.valueOf( networkCodeDetails.get( bnumber ).getCall_type() );
						return calltype;
					}
					i--;
					continue;
				}
			}

		}
		return "9";
	}
}
