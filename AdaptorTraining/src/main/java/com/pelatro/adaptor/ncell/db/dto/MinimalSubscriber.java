package com.pelatro.adaptor.ncell.db.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table( name = "SUBSCRIBER" )
@DynamicUpdate
public class MinimalSubscriber {

	@Id @Column( name = "ID" ) private int id;
	@Column( name = "MSISDN" ) private String msisdn;
	@Column( name = "IS_DELETED" ) private int is_deleted;

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

	public int getIs_deleted() {
		return is_deleted;
	}

	public void setIs_deleted( int is_deleted ) {
		this.is_deleted = is_deleted;
	}
}
