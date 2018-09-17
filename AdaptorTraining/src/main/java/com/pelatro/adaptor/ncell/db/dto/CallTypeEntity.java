package com.pelatro.adaptor.ncell.db.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table( name = "NCELL_CALL_TYPE_DETAILS" )
public class CallTypeEntity {

	@Id @Column( name = "USAGESERVICETYPE" ) private String usageservicetype;
	@Column( name = "DESCRIPTION" ) private String description;
	@Column( name = "CALL_TYPE" ) private Integer call_type;
	@Column( name = "OPERATOR_ID" ) private Integer operator_id;

	public String getUsageservicetype() {
		return usageservicetype;
	}

	public void setUsageservicetype( String usageservicetype ) {
		this.usageservicetype = usageservicetype;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription( String description ) {
		this.description = description;
	}

	public Integer getCall_type() {
		return call_type;
	}

	public void setCall_type( Integer call_type ) {
		this.call_type = call_type;
	}

	public Integer getOperator_id() {
		return operator_id;
	}

	public void setOperator_id( Integer operator_id ) {
		this.operator_id = operator_id;
	}
}
