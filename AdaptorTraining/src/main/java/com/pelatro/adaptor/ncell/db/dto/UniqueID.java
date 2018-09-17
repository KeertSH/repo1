package com.pelatro.adaptor.ncell.db.dto;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
@SequenceGenerator(
		name = "uid-seq",
		sequenceName = "ADAPTOR_UID_SEQ",
		allocationSize = 100 )
public class UniqueID {

	@Id @GeneratedValue( strategy = GenerationType.SEQUENCE,
			generator = "uid-seq" ) private long id;

	public long getId() {
		return id;
	}

	public void setId( long id ) {
		this.id = id;
	}
}