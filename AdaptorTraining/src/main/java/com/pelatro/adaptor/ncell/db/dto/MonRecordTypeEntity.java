package com.pelatro.adaptor.ncell.db.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table( name = "MON_RECORD_TYPE" )
public class MonRecordTypeEntity {

	@Id @Column( name = "ID" ) private int id;
	@Column( name = "PRODUCT_ID" ) private String product_id;
	@Column( name = "PRODUCT_NAME" ) private String product_name;
	@Column( name = "RECORD_TYPE" ) private int record_type;

	public int getId() {
		return id;
	}

	public void setId( int id ) {
		this.id = id;
	}

	public String getProduct_id() {
		return product_id;
	}

	public void setProduct_id( String product_id ) {
		this.product_id = product_id;
	}

	public String getProduct_name() {
		return product_name;
	}

	public void setProduct_name( String product_name ) {
		this.product_name = product_name;
	}

	public int getRecord_type() {
		return record_type;
	}

	public void setRecord_type( int record_type ) {
		this.record_type = record_type;
	}
}
