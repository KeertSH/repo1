package com.pelatro.adaptor.ncell.db.dto;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
@SequenceGenerator(
		name = "gpfreference-seq",
		sequenceName = "GPFREFERENCE_SEQ",
		allocationSize = 100 )
public class GpfUniqueID {

	@Id @GeneratedValue( strategy = GenerationType.SEQUENCE,
			generator = "gpfreference-seq" ) private int id;

	public int getId() {
		return id;
	}

	public void setId( int id ) {
		this.id = id;
	}
}
