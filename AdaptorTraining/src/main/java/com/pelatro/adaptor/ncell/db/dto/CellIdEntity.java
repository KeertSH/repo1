package com.pelatro.adaptor.ncell.db.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table( name = "NCELL_CELL_ID_DETAILS" )
public class CellIdEntity {

	@Id @Column( name = "CELLID" ) private String cellid;
	@Column( name = "PROVINCE" ) private String province;
	@Column( name = "DISTRICT" ) private String district;
	@Column( name = "AREA" ) private String area;
	@Column( name = "LAC_CELLID" ) private String lac_cellid;

	public String getLac_cellid() {
		return lac_cellid;
	}

	public void setLac_cellid( String lac_cellid ) {
		this.lac_cellid = lac_cellid;
	}

	public String getCellid() {
		return cellid;
	}

	public void setCellid( String cellid ) {
		this.cellid = cellid;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince( String province ) {
		this.province = province;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict( String district ) {
		this.district = district;
	}

	public String getArea() {
		return area;
	}

	public void setArea( String area ) {
		this.area = area;
	}
}
