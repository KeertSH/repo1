package com.pelatro.adaptor.ncell.db.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table( name = "PERIOD" )
public class Period {

	@Id @Column( name = "PERIOD_TYPE" ) private String periodType;
	@Column( name = "PERIOD" ) private String period;

	public String getPeriod() {
		return period;
	}

	public void setPeriod( String period ) {
		this.period = period;
	}

	public String getPeriodType() {
		return periodType;
	}

	public void setPeriodType( String periodType ) {
		this.periodType = periodType;
	}

	public String period() {
		return period;
	}
}
