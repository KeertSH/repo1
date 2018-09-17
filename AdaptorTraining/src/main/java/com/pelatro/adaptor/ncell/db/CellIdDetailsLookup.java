package com.pelatro.adaptor.ncell.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.StatelessSession;

import com.pelatro.adaptor.ncell.db.dto.CellIdEntity;

public class CellIdDetailsLookup {

	private static Logger logger = LogManager.getLogger( CellIdDetailsLookup.class );

	private Query query;
	private StatelessSession session = null;
	private List<CellIdEntity> cellIdDetails;
	private Map<String, CellIdEntity> cellIdDetailsMap = new HashMap<>();

	@SuppressWarnings( "unchecked" )
	public CellIdDetailsLookup() {
		logger.info( "Constructing CellIdDetailsDB." );
		session =
				DbHenchman.getHenchman().getSessionFactory().openStatelessSession();
		query = session.createQuery( "FROM CellIdEntity" );
		cellIdDetails = query.list();

		for ( CellIdEntity cellIdEntity : cellIdDetails ) {
			cellIdDetailsMap.put( cellIdEntity.getLac_cellid(), cellIdEntity );
		}

		session.close();
	}

	public CellIdEntity getCellIdDetails( String raw ) {
		CellIdEntity cellIdEntity = cellIdDetailsMap.get( raw );
		return cellIdEntity;
	}
}
