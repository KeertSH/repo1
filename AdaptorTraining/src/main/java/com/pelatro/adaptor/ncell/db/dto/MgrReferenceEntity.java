package com.pelatro.adaptor.ncell.db.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table( name = "mgr_reference_table" )
public class MgrReferenceEntity {

	@Id @Column( name = "GPF_ID" ) private int gpf_id;
	@Column( name = "OPERATIONTYPE" ) private String operationtype;
	@Column( name = "TAGS" ) private String tags;

	public int getGpf_id() {
		return gpf_id;
	}

	public void setGpf_id( int gpf_id ) {
		this.gpf_id = gpf_id;
	}

	public String getOperationtype() {
		return operationtype;
	}

	public void setOperationtype( String operationtype ) {
		this.operationtype = operationtype;
	}

	public String getTags() {
		return tags;
	}

	public void setTags( String tags ) {
		this.tags = tags;
	}
}
