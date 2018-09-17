package com.pelatro.adaptor.ncell.db.dto;

public class Subscriber {

	private int id;
	private String msisdn;
	private byte status;
	private int mainProduct;
	private long doj;
	private byte type;
	private String subscriberkey;
	private String accountkey;

	public String getAccountkey() {
		return accountkey;
	}

	public void setAccountkey( String accountkey ) {
		this.accountkey = accountkey;
	}

	public String getSubscriberkey() {
		return subscriberkey;
	}

	public void setSubscriberkey( String subscriberkey ) {
		this.subscriberkey = subscriberkey;
	}

	public int getId() {
		return id;
	}

	public void setId( int id ) {
		this.id = id;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn( String msisdn ) {
		this.msisdn = msisdn;
	}

	public byte getStatus() {
		return status;
	}

	public void setStatus( byte status ) {
		this.status = status;
	}

	public int getMainProduct() {
		return mainProduct;
	}

	public void setMainProduct( int mainProduct ) {
		this.mainProduct = mainProduct;
	}

	public long getDoj() {
		return doj;
	}

	public void setDoj( long doj ) {
		this.doj = doj;
	}

	public byte getType() {
		return type;
	}

	public void setType( byte type ) {
		this.type = type;
	}
}
