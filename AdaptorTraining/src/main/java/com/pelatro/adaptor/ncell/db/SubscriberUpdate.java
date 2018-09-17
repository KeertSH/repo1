package com.pelatro.adaptor.ncell.db;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.pelatro.adaptor.ncell.db.dto.Subscriber;

public class SubscriberUpdate {

	static Transaction tx = null;

	public static void updateToPostpaid( Integer id, String msisdn ) {
		Session session =
				DbHenchman.getHenchman().getSessionFactory().openSession();
		tx = session.beginTransaction();
		org.hibernate.Query query = session.createSQLQuery(
				"update subscriber  set type = 1, is_deleted = 1"
						+ " where msisdn = :msisdn "
						+ " and id = :id" );
		query.setInteger( "id", id );
		query.setString( "msisdn", msisdn );
		query.executeUpdate();
		session.flush();
		session.clear();
		tx.commit();
		session.close();
	}

	public static void updateToPrepaid( Integer id, String msisdn ) {
		Session session =
				DbHenchman.getHenchman().getSessionFactory().openSession();
		tx = session.beginTransaction();
		org.hibernate.Query query = session.createSQLQuery(
				"update subscriber  set type = 0, is_deleted = 0"
						+ " where msisdn = :msisdn "
						+ " and id = :id" );
		query.setInteger( "id", id );
		query.setString( "msisdn", msisdn );
		query.executeUpdate();
		session.flush();
		session.clear();
		tx.commit();
		session.close();
	}

	public static void updateSubscriberKey( Subscriber sub ) {
		Session session =
				DbHenchman.getHenchman().getSessionFactory().openSession();
		tx = session.beginTransaction();
		org.hibernate.Query query = session.createSQLQuery(
				"update subscriber  set is_deleted = 1 "
						+ " where msisdn = :msisdn "
						+ " and id = :id"
						+ " and status = :status "
						+ " and main_product = :main_product "
						+ " and subscriberkey = :subscriberkey "
						+ " and type = :type "
						+ " and account_key = :account_key" );
		query.setInteger( "id", sub.getId() );
		query.setString( "msisdn", sub.getMsisdn() );
		query.setInteger( "status", sub.getStatus() );
		query.setString( "main_product", String.valueOf( sub.getMainProduct() ) );
		query.setString( "subscriberkey", sub.getSubscriberkey() );
		query.setInteger( "type", sub.getType() );
		query.setString( "account_key", sub.getAccountkey() );
		query.executeUpdate();
		tx.commit();
		session.close();
	}
}
