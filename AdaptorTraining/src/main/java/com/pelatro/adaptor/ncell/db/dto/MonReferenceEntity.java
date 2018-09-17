package com.pelatro.adaptor.ncell.db.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table( name = "mon_reference_table" )
public class MonReferenceEntity {

	@Id @Column( name = "GPF_ID" ) private int gpf_id;
	@Column( name = "PRODUCT_ID" ) private String product_id;
	@Column( name = "TAGS" ) private String tags;

	public int getGpf_id() {
		return gpf_id;
	}

	public void setGpf_id( int gpf_id ) {
		this.gpf_id = gpf_id;
	}

	public String getProduct_id() {
		return product_id;
	}

	public void setProduct_id( String product_id ) {
		this.product_id = product_id;
	}

	public String getTags() {
		return tags;
	}

	public void setTags( String tags ) {
		this.tags = tags;
	}
}
