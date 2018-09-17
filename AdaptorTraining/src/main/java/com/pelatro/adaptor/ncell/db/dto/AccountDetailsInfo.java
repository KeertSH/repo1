package com.pelatro.adaptor.ncell.db.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table( name = "account_balance_info" )
public class AccountDetailsInfo {

	@Id @Column( name = "id" ) private int id;
	@Column( name = "accountid" ) private int accountid;
	@Column( name = "balancetype" ) private String balancetype;

	public int getId() {
		return id;
	}

	public void setId( int id ) {
		this.id = id;
	}

	public int getAccountid() {
		return accountid;
	}

	public void setAccountid( int accountid ) {
		this.accountid = accountid;
	}

	public String getBalancetype() {
		return balancetype;
	}

	public void setBalancetype( String balancetype ) {
		this.balancetype = balancetype;
	}
}
