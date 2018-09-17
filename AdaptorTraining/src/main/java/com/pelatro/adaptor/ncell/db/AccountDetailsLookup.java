package com.pelatro.adaptor.ncell.db;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.StatelessSession;

import com.pelatro.adaptor.ncell.common.Constants;
import com.pelatro.adaptor.ncell.db.dto.AccountDetailsInfo;

public class AccountDetailsLookup {

	private static final List<AccountDetailsInfo> accountinfo = new ArrayList<>();
	private static final HashMap<Integer, String> accountDetails =
			new HashMap<>();
	private static final Logger logger = LogManager.getLogger( AccountDetailsInfo.class );
	static DecimalFormat formatter = new DecimalFormat( "0.0000" );
	private static String accountID = Constants.AccountID;
	private static StatelessSession session = null;


	public static String getAccountID() {
		return accountID;
	}

	public static String getUnit() {
		return unit;
	}

	public static String getBalance_value() {
		return balance_value;
	}

	public static String getAccountID1() {
		return accountID1;
	}

	public static String getUnit1() {
		return unit1;
	}

	public static String getBalance_value1() {
		return balance_value1;
	}

	public static String getAccountID2() {
		return accountID2;
	}

	public static String getUnit2() {
		return unit2;
	}

	public static String getBalance_value2() {
		return balance_value2;
	}

	private static String unit = Constants.Unit;
	private static String balance_value = Constants.AccountBalance;
	private static String accountID1 = Constants.AccountID;
	private static String unit1 = Constants.Unit;
	private static String balance_value1 = Constants.AccountBalance;
	private static String accountID2 = Constants.AccountID;
	private static String unit2 = Constants.Unit;
	private static String balance_value2 = Constants.AccountBalance;

	public static void initialize() {

		session =
				DbHenchman.getHenchman().getSessionFactory().openStatelessSession();

		logger.info( "Loading Account Info Details..." );

		Query query = session.createQuery( " from AccountDetailsInfo " );
		@SuppressWarnings( "unchecked" )
		List<AccountDetailsInfo> AccountDetailsList = query.list();

		logger.info(
				String.format( "... %d Account Details slurped ...",
						AccountDetailsList.size() ) );

		accountinfo.clear();
		for ( AccountDetailsInfo account : AccountDetailsList )
			accountinfo.add( account );
		for ( int i = 0; i < accountinfo.size(); i++ ) {
			accountDetails.put( accountinfo.get( i ).getAccountid(),
					accountinfo.get( i ).getBalancetype() );
		}

		logger.info( String.format( "... %d Account Details loaded ..", accountinfo.size() ) );

		session.close();
	}

	public static void accountIdlookup( ArrayList<AccountInfo> accountList ) {
		accountID = Constants.AccountID;
		unit = Constants.Unit;
		balance_value = Constants.AccountBalance;
		accountID1 = Constants.AccountID;
		unit1 = Constants.Unit;
		balance_value1 = Constants.AccountBalance;
		accountID2 = Constants.AccountID;
		unit2 = Constants.Unit;
		balance_value2 = Constants.AccountBalance;

		for ( int i = 0; i < accountList.size(); i++ ) {
			if ( accountDetails.containsKey( accountList.get( i ).getAccountID() ) ) {

				accountID = accountList.get( i ).getAccountID().toString();
				unit = accountDetails.get( Integer.parseInt( accountID ) );
				balance_value = formatter.format( accountList.get( i ).getAccountBalance() );
				accountList.remove( i );
				break;
			}
		}

		if ( !accountList.isEmpty() ) {
			for ( int i = 0; i < accountList.size(); i++ ) {
				if ( accountDetails.containsKey( accountList.get( i ).getAccountID() ) ) {

					accountID1 = accountList.get( i ).getAccountID().toString();
					unit1 = accountDetails.get( Integer.parseInt( accountID1 ) );
					balance_value1 =
							formatter.format( accountList.get( i ).getAccountBalance() );
					accountList.remove( i );
					break;
				}
			}
		}
		if ( !accountList.isEmpty() ) {
			for ( int i = 0; i < accountList.size(); i++ ) {
				if ( accountDetails.containsKey( accountList.get( i ).getAccountID() ) ) {

					accountID2 = accountList.get( i ).getAccountID().toString();
					unit2 = accountDetails.get( Integer.parseInt( accountID2 ) );
					balance_value2 =
							formatter.format( accountList.get( i ).getAccountBalance() );
					accountList.remove( i );
					break;
				}
			}
		}

	}

}
