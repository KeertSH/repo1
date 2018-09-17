package com.pelatro.adaptor.ncell.db;

import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StatelessSession;

import com.pelatro.adaptor.ncell.db.dto.ComReferenceEntity;

public class ComGpfLookup {

	private static Logger logger = LogManager.getLogger( ComGpfLookup.class );

	private static final HashMap<String, ComReferenceEntity> comReferenceDetailsMap =
			new HashMap<>();

	private static StatelessSession session = null;
	private Query query;

	private List<ComReferenceEntity> comReferenceList;

	@SuppressWarnings( "unchecked" )
	public ComGpfLookup() {

		session =
				DbHenchman.getHenchman().getSessionFactory().openStatelessSession();
		logger.info( "Loading Com Reference Details..." );

		query = session.createQuery(
				" from ComReferenceEntity" );
		comReferenceList = query.list();

		for ( ComReferenceEntity comReferenceEntity : comReferenceList ) {
			comReferenceDetailsMap.put( comReferenceEntity.getContent_id(),
					comReferenceEntity );
		}
		session.close();
	}

	public int gpfidlookup( String contentId ) {
		int gpf_id = -1;
		if ( comReferenceDetailsMap.containsKey( contentId ) ) {
			gpf_id = comReferenceDetailsMap.get( contentId ).getGpf_id();
			return gpf_id;
		}
		return gpf_id;
	}

	public void save( int newid, String contentId ) {
		Session session =
				DbHenchman.getHenchman().getSessionFactory().openSession();
		session.beginTransaction();
		ComReferenceEntity cr = new ComReferenceEntity();
		cr.setGpf_id( newid );
		cr.setContent_id( contentId );
		session.save( cr );
		session.getTransaction().commit();
		session.close();
		comReferenceDetailsMap.put( contentId, cr );
	}
}
