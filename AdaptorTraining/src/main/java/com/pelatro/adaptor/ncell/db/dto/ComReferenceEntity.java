package com.pelatro.adaptor.ncell.db.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table( name = "com_reference_table" )
public class ComReferenceEntity {

	@Id @Column( name = "GPF_ID" ) private int gpf_id;
	@Column( name = "CONTENT_ID" ) private String content_id;
	@Column( name = "TAGS" ) private String tags;

	public int getGpf_id() {
		return gpf_id;
	}

	public void setGpf_id( int gpf_id ) {
		this.gpf_id = gpf_id;
	}

	public String getContent_id() {
		return content_id;
	}

	public void setContent_id( String content_id ) {
		this.content_id = content_id;
	}

	public String getTags() {
		return tags;
	}

	public void setTags( String tags ) {
		this.tags = tags;
	}
}
