package com.pelatro.adaptor.ncell.db;

import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.StatelessSession;

import com.pelatro.adaptor.ncell.common.Constants;
import com.pelatro.adaptor.ncell.db.dto.OnNetNumberPrefixEntity;

public class NumberPrefixLookup {

	private static final HashMap<String, OnNetNumberPrefixEntity> networkCodeDetails = new HashMap<>();
	private static Logger logger = LogManager.getLogger(NumberPrefixLookup.class);

	private Query query;
	private StatelessSession session = null;
	private List<OnNetNumberPrefixEntity> onnetCodeList;

	@SuppressWarnings("unchecked")
	public NumberPrefixLookup() {

		session = DbHenchman.getHenchman().getSessionFactory().openStatelessSession();

		logger.info("Loading NetworkCode and Operator Details...");

		query = session.createQuery(" from OnNetNumberPrefixEntity ");
		onnetCodeList = query.list();

		logger.info(String.format("... %d NetworkCode slurped ...", onnetCodeList.size()));

		for (int i = 0; i < onnetCodeList.size(); i++) {
			networkCodeDetails.put(onnetCodeList.get(i).getPrefix(), onnetCodeList.get(i));
		}

		logger.info(String.format("... %d NetworkCode registered..", onnetCodeList.size()));

		session.close();
	}

	public String operatorIdlookup(String bnumber) {
		if (bnumber.startsWith(Constants.NCELL_COUNTRY_CODE)) {
			bnumber = bnumber.substring(3, bnumber.length());
			int i = bnumber.length();
			while (i > 0) {
				bnumber = bnumber.substring(0, i);
				if (networkCodeDetails.containsKey(bnumber)) {
					String operatorid = String.valueOf(networkCodeDetails.get(bnumber).getOperator_id());
					return operatorid;
				}
				i--;
				continue;
			}
		}
		return "9";
	}

	public String callTypelookup(String bnumber) {
		if (bnumber.startsWith(Constants.NCELL_COUNTRY_CODE)) {
			bnumber = bnumber.substring(3, bnumber.length());
			if (networkCodeDetails.containsKey(bnumber)) {
				String calltype = String.valueOf(networkCodeDetails.get(bnumber).getCall_type());
				return calltype;
			} else {
				int i = bnumber.length();
				while (i > 0) {
					bnumber = bnumber.substring(0, i);
					if (networkCodeDetails.containsKey(bnumber)) {
						String calltype = String.valueOf(networkCodeDetails.get(bnumber).getCall_type());
						return calltype;
					}
					i--;
					continue;
				}
			}

		}
		return "9";
	}
}
