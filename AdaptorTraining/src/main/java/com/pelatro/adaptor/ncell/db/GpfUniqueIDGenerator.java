package com.pelatro.adaptor.ncell.db;

import org.hibernate.Session;

import com.pelatro.adaptor.ncell.db.dto.GpfUniqueID;

public class GpfUniqueIDGenerator {

	private static Session session = null;
	private static int generated;

	public static void open() {
		if ( session != null )
			session.close();

		session = DbHenchman.getHenchman().getSessionFactory().openSession();
	}

	public static int generate() {
		GpfUniqueID uid = new GpfUniqueID();
		session.persist( uid );
		++generated;

		if ( generated >= 5000 ) {
			generated = 0;
			session.clear();
		}
		return uid == null ? -1 : uid.getId();
	}

	public static void close() {
		if ( session == null )
			return;

		session.close();
		session = null;
	}
}
