package com.pelatro.adaptor.ncell.db.dto;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table( name = "SUBSCRIBER" )
public class CompleteSubscriber {

	@Id @Column( name = "ID" ) private int id;
	@Column( name = "MSISDN" ) private String msisdn;
	@Column( name = "DOJ" ) private Timestamp dateOfJoining;
	@Column( name = "STATUS" ) private int status;
	@Column( name = "IS_DELETED" ) private int isDeleted;
	@Column( name = "IMSI" ) private String IMSI;
	@Column( name = "LANG" ) private String language;
	@Column( name = "MAIN_PRODUCT" ) private String mainProduct;
	@Column( name = "PREF_CHANNEL" ) private Integer prefChannel;
	@Column( name = "EMAIL_ID" ) private String email_id;
	@Column( name = "VERIFIED_EMAIL_ID" ) private int verified_email_id;
	@Column( name = "LAST_RECHARGE_DATE" ) private String last_recharge_date;
	@Column( name = "TYPE" ) private int type;
	@Column( name = "SUBSCRIBERKEY" ) private String subscriberkey;
	@Column( name = "ACCOUNT_KEY" ) private String account_Key;

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

	public Timestamp getDateOfJoining() {
		return dateOfJoining;
	}

	public void setDateOfJoining( Timestamp dateOfJoining ) {
		this.dateOfJoining = dateOfJoining;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus( int status ) {
		this.status = status;
	}

	public int getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted( int isDeleted ) {
		this.isDeleted = isDeleted;
	}

	public String getIMSI() {
		return IMSI;
	}

	public void setIMSI( String iMSI ) {
		IMSI = iMSI;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage( String language ) {
		this.language = language;
	}

	public String getMainProduct() {
		return mainProduct;
	}

	public void setMainProduct( String mainProduct ) {
		this.mainProduct = mainProduct;
	}

	public Integer getPrefChannel() {
		return prefChannel;
	}

	public void setPrefChannel( Integer prefChannel ) {
		this.prefChannel = prefChannel;
	}

	public String getEmail_id() {
		return email_id;
	}

	public void setEmail_id( String email_id ) {
		this.email_id = email_id;
	}

	public int getVerified_email_id() {
		return verified_email_id;
	}

	public void setVerified_email_id( int verified_email_id ) {
		this.verified_email_id = verified_email_id;
	}

	public String getLast_recharge_date() {
		return last_recharge_date;
	}

	public void setLast_recharge_date( String last_recharge_date ) {
		this.last_recharge_date = last_recharge_date;
	}

	public int getType() {
		return type;
	}

	public void setType( int type ) {
		this.type = type;
	}

	public String getSubscriberkey() {
		return subscriberkey;
	}

	public void setSubscriberkey( String subscriberkey ) {
		this.subscriberkey = subscriberkey;
	}

	public String getAccount_Key() {
		return account_Key;
	}

	public void setAccount_Key( String account_Key ) {
		this.account_Key = account_Key;
	}
}
