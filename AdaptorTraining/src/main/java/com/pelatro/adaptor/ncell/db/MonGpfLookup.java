package com.pelatro.adaptor.ncell.db;

import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StatelessSession;

import com.pelatro.adaptor.ncell.db.dto.MonReferenceEntity;

public class MonGpfLookup {

	private static Logger logger = LogManager.getLogger( MonGpfLookup.class );

	private static final HashMap<String, MonReferenceEntity> monReferenceDetailsMap =
			new HashMap<>();

	private static StatelessSession session = null;
	private Query query;

	private List<MonReferenceEntity> monReferenceList;

	@SuppressWarnings( "unchecked" )
	public MonGpfLookup() {

		session =
				DbHenchman.getHenchman().getSessionFactory().openStatelessSession();
		logger.info( "Loading Mon Reference Details..." );

		query = session.createQuery(
				" from MonReferenceEntity" );
		monReferenceList = query.list();

		for ( MonReferenceEntity monReferenceEntity : monReferenceList ) {
			monReferenceDetailsMap.put( monReferenceEntity.getProduct_id(),
					monReferenceEntity );
		}
		session.close();
	}

	public int gpfidlookup( String productId ) {
		int gpf_id = -1;
		if ( monReferenceDetailsMap.containsKey( productId ) ) {
			gpf_id = monReferenceDetailsMap.get( productId ).getGpf_id();
			return gpf_id;
		}
		return gpf_id;
	}

	public static void save( int newid, String productId ) {
		Session session =
				DbHenchman.getHenchman().getSessionFactory().openSession();
		session.beginTransaction();
		MonReferenceEntity cr = new MonReferenceEntity();
		cr.setGpf_id( newid );
		cr.setProduct_id( productId );
		session.save( cr );
		session.getTransaction().commit();
		session.close();
		monReferenceDetailsMap.put( productId, cr );
	}
}
