package com.pelatro.adaptor.ncell.db;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.StatelessSession;

import com.pelatro.adaptor.ncell.db.dto.MinimalSubscriber;

public class SubscriberDBUpdate {

	private static Logger logger = LogManager.getLogger( SubscriberDBUpdate.class );

	private final int nThreads;
	private DBWriter[] writers;
	private Thread[] threads;

	public SubscriberDBUpdate( int nThreads ) {
		this.nThreads = nThreads;
		this.writers = new DBWriter[this.nThreads];
		this.threads = new Thread[this.nThreads];
	}

	public void start() {
		for ( int i = 0; i < nThreads; ++i ) {
			writers[i] = new DBWriter();
			threads[i] = new Thread( writers[i] );
			threads[i].start();
		}
	}

	public void update( MinimalSubscriber row ) throws InterruptedException {
		int turn = Integer.parseInt( row.getMsisdn().substring( 6 ) ) % nThreads;
		writers[turn].update( row );
	}

	public void shutdown() throws InterruptedException {
		for ( DBWriter writer : writers )
			writer.stop();

		for ( Thread thread : threads )
			thread.join();

		for ( DBWriter writer : writers )
			writer.close();
	}

	private static class DBWriter implements Runnable, AutoCloseable {

		private final StatelessSession session;
		private final BlockingQueue<MinimalSubscriber> queue;

		private boolean stop;
		private static final int LIMIT = 5000;

		public DBWriter() {
			session = DbHenchman.getHenchman().getSessionFactory().openStatelessSession();
			queue = new LinkedBlockingQueue<>( LIMIT );
			stop = false;
		}

		private void update( MinimalSubscriber row ) throws InterruptedException {
			queue.put( row );
		}

		public synchronized void stop() {
			stop = true;
		}

		@Override
		public void run() {
			session.beginTransaction();
			int nonCommitCount = 0;

			try {
				while ( !stop || !queue.isEmpty() ) {
					MinimalSubscriber row = null;
					do {
						row = queue.poll( 100, TimeUnit.MILLISECONDS );
					}
					while ( row == null && !stop );

					if ( row == null )
						break;

					if ( row.getId() < 0 ) {
						row.setId( -row.getId() );
						try {
							session.insert( row );
						}
						catch ( Exception e ) {
							logger.error( String.format(
									"Error inserting subscriber with ID: %d and MSISDN: %s",
									row.getId(), row.getMsisdn() ) );
							throw e;
						}
					}
					else {
						try {
							session.update( row );
						}
						catch ( Exception e ) {
							logger.error( String.format(
									"Error updating subscriber with ID: %d and MSISDN: %s",
									row.getId(), row.getMsisdn() ) );
							throw e;
						}
					}

					if ( ++nonCommitCount % 1000 == 0 ) {
						logger.debug( String.format( "Commits: %d", nonCommitCount ) );
						session.getTransaction().commit();
						session.beginTransaction();
					}
				}
			}
			catch ( InterruptedException e ) {
				e.printStackTrace();
			}

			if ( nonCommitCount % 500 != 0 ) {
				logger.debug( String.format( "Commits: %d", nonCommitCount ) );
				session.getTransaction().commit();
			}
		}

		@Override
		public void close() {
			session.close();
		}
	}
}
