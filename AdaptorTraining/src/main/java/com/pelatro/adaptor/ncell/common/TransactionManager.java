package com.pelatro.adaptor.ncell.common;

import org.hibernate.Session;

import com.pelatro.adaptor.ncell.db.DbHenchman;

public class TransactionManager {

	private int txCounter;
	private final int commitCount;

	public Session session;

	public TransactionManager() {
		this( 1000 );
	}

	public TransactionManager( int commitCount ) {
		this.commitCount = commitCount;

		this.session = null;
		txCounter = 0;
	}

	public void open() {
		if ( session == null )
			this.session = DbHenchman.getHenchman().getSessionFactory().openSession();

		if ( !session.getTransaction().isActive() )
			begin();
	}

	public void begin() {
		session.beginTransaction();
	}

	public void increment() {
		if ( ++txCounter < commitCount )
			return;

		txCounter = 0;
		if ( !session.getTransaction().wasCommitted() )
			session.getTransaction().commit();
		begin();
	}

	public void end() {
		if ( !session.getTransaction().wasCommitted() )
			session.getTransaction().commit();
	}

	public void close() {
		end();
		session.close();
		session = null;
	}
}
