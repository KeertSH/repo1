package com.pelatro.adaptor.ncell.db.dto;

import java.util.Comparator;

import com.pelatro.adaptor.ncell.db.AccountInfo;

public class CustomComparator implements Comparator<AccountInfo> {

	@Override
	public int compare( AccountInfo o1, AccountInfo o2 ) {
		return o1.getAccountBalance().compareTo( o2.getAccountBalance() );
	}
}
