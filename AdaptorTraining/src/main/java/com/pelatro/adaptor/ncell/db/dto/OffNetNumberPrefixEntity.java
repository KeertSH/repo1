package com.pelatro.adaptor.ncell.db.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table( name = "ncell_offnet_call_type_details" )
public class OffNetNumberPrefixEntity {

	@Id @Column( name = "ID" ) private int id;
	@Column( name = "COUNTRY_CODE" ) private String country_code;
	@Column( name = "PREFIX" ) private String prefix;
	@Column( name = "NETWORKCODE" ) private String networkcode;
	@Column( name = "CALL_TYPE" ) private int call_type;
	@Column( name = "OPERATOR_ID" ) private int operator_id;

	public int getId() {
		return id;
	}

	public void setId( int id ) {
		this.id = id;
	}

	public String getCountry_code() {
		return country_code;
	}

	public void setCountry_code( String country_code ) {
		this.country_code = country_code;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix( String prefix ) {
		this.prefix = prefix;
	}

	public String getNetworkcode() {
		return networkcode;
	}

	public void setNetwork_code( String networkcode ) {
		this.networkcode = networkcode;
	}

	public int getCall_type() {
		return call_type;
	}

	public void setCall_type( int call_type ) {
		this.call_type = call_type;
	}

	public int getOperator_id() {
		return operator_id;
	}

	public void setOperator_id( int operator_id ) {
		this.operator_id = operator_id;
	}
}
