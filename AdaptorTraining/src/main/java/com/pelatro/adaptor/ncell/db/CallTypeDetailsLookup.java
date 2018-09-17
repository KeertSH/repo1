package com.pelatro.adaptor.ncell.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.StatelessSession;

import com.pelatro.adaptor.ncell.db.dto.CallTypeEntity;

public class CallTypeDetailsLookup {

	private static Logger logger = LogManager.getLogger( CallTypeDetailsLookup.class );

	private List<CallTypeEntity> callTypeDetails;
	private Query query;
	private StatelessSession session = null;
	private Map<String, CallTypeEntity> callTypeMap = new HashMap<>();

	@SuppressWarnings( "unchecked" )
	public CallTypeDetailsLookup() {
		logger.info( "Constructing CallTypeDetailsDB." );
		session =
				DbHenchman.getHenchman().getSessionFactory().openStatelessSession();
		query = session.createQuery( "FROM CallTypeEntity" );
		callTypeDetails = query.list();

		for ( CallTypeEntity callTypeEntity : callTypeDetails ) {
			callTypeMap.put( callTypeEntity.getUsageservicetype(), callTypeEntity );
		}

		session.close();
	}

	public Integer getCallType( String raw ) {

		if ( callTypeMap.containsKey( raw ) ) {
			return callTypeMap.get( raw ).getCall_type();
		}
		else {
			return 9;
		}
	}

	public Integer getOperator( String raw ) {
		if ( callTypeMap.containsKey( raw ) ) {
			return callTypeMap.get( raw ).getOperator_id();
		}
		else {
			return 9;
		}

	}
}
