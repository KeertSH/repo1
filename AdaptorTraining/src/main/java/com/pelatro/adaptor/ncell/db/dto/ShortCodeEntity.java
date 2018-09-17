package com.pelatro.adaptor.ncell.db.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table( name = "SHORT_CODE_DETAILS" )
public class ShortCodeEntity {

	@Id @Column( name = "SHORT_CODE_VALUE" ) private String short_code_value;

	public String getShort_code_value() {
		return short_code_value;
	}

	public void setShort_code_value( String short_code_value ) {
		this.short_code_value = short_code_value;
	}
}
