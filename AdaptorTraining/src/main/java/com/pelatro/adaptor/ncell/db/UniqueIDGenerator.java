package com.pelatro.adaptor.ncell.db;

import org.hibernate.Session;

import com.pelatro.adaptor.ncell.db.dto.UniqueID;

public class UniqueIDGenerator {

	private static Session session = null;
	private static int generated;

	public static void open() {
		if ( session != null )
			session.close();

		session = DbHenchman.getHenchman().getSessionFactory().openSession();
	}

	public static long generate() {
		UniqueID uid = new UniqueID();
		session.persist( uid );
		++generated;

		if ( generated >= 5000 ) {
			generated = 0;
			session.clear();
		}
		return uid == null ? -1L : Long.reverse( uid.getId() );
	}

	public static void close() {
		if ( session == null )
			return;

		session.close();
		session = null;
	}
}
