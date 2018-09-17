package com.pelatro.adaptor.ncell.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.StatelessSession;

import com.pelatro.adaptor.ncell.common.Constants;
import com.pelatro.adaptor.ncell.db.dto.MonRecordTypeEntity;

public class MonRecordTypeLookup {

	private static Logger logger = LogManager.getLogger( MonRecordTypeLookup.class );

	private List<MonRecordTypeEntity> recordTypeDetails;
	private Query query;
	private static StatelessSession session = null;

	private Map<String, MonRecordTypeEntity> recordTypeMap = new HashMap<>();

	@SuppressWarnings( "unchecked" )
	public MonRecordTypeLookup() {
		logger.info( "Constructing RecordTypeDetailsDB." );
		session =
				DbHenchman.getHenchman().getSessionFactory().openStatelessSession();
		query = session.createQuery( "FROM MonRecordTypeEntity" );
		recordTypeDetails = query.list();

		for ( MonRecordTypeEntity recordTypeEntity : recordTypeDetails ) {
			recordTypeMap.put( recordTypeEntity.getProduct_id(), recordTypeEntity );
		}

		session.close();
	}

	public Integer getRecordType( String raw ) {

		if ( recordTypeMap.containsKey( raw ) ) {
			return recordTypeMap.get( raw ).getRecord_type();
		}
		return 9;
	}

	public String getProductName( String raw ) {
		if ( recordTypeMap.containsKey( raw ) ) {
			return recordTypeMap.get( raw ).getProduct_name();
		}
		return Constants.EMPTY;
	}
}
