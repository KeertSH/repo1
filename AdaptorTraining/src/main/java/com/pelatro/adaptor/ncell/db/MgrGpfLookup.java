package com.pelatro.adaptor.ncell.db;

import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StatelessSession;

import com.pelatro.adaptor.ncell.db.dto.MgrReferenceEntity;

public class MgrGpfLookup {

	private static Logger logger = LogManager.getLogger( MgrGpfLookup.class );

	private static final HashMap<String, MgrReferenceEntity> mgrReferenceDetailsMap =
			new HashMap<>();

	private static StatelessSession session = null;
	private Query query;

	private List<MgrReferenceEntity> mgrReferenceList;

	@SuppressWarnings( "unchecked" )
	public MgrGpfLookup() {

		session =
				DbHenchman.getHenchman().getSessionFactory().openStatelessSession();
		logger.info( "Loading Mon Reference Details..." );

		query = session.createQuery(
				" from MgrReferenceEntity" );
		mgrReferenceList = query.list();

		for ( MgrReferenceEntity mgrReferenceEntity : mgrReferenceList ) {
			mgrReferenceDetailsMap.put( mgrReferenceEntity.getOperationtype(),
					mgrReferenceEntity );
		}
		session.close();
	}

	public int gpfidlookup( String productId ) {
		int gpf_id = -1;
		if ( mgrReferenceDetailsMap.containsKey( productId ) ) {
			gpf_id = mgrReferenceDetailsMap.get( productId ).getGpf_id();
			return gpf_id;
		}
		return gpf_id;
	}

	public void save( int newid, String operationType ) {
		Session session =
				DbHenchman.getHenchman().getSessionFactory().openSession();
		session.beginTransaction();
		MgrReferenceEntity cr = new MgrReferenceEntity();
		cr.setGpf_id( newid );
		cr.setOperationtype( operationType );
		session.save( cr );
		session.getTransaction().commit();
		session.close();
		mgrReferenceDetailsMap.put( operationType, cr );
	}
}
