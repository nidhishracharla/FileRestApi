package com.ge.capital.dms.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "TB_COUNTY_LIST")

public class CountyList implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	@Id
	@Column(name = "county_code", unique = true, nullable = false, length = 10)
	private String countyCode;
	@Column(name = "county_group", unique = false, nullable = false, length = 30)
	private String countyGroup;
	@Column(name = "county_name", unique = false, nullable = false, length = 50)
	private String countyName;
	@Column(name = "state", unique = false, nullable = false, length = 50)
	private String state;
	
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getCountyCode() {
		return countyCode;
	}
	public void setCountyCode(String countyCode) {
		this.countyCode = countyCode;
	}
	public String getCountyName() {
		return countyName;
	}
	public void setCountyName(String countyName) {
		this.countyName = countyName;
	}
	public String getCountyGroup() {
		return countyGroup;
	}
	public void setCountyGroup(String countyGroup) {
		this.countyGroup = countyGroup;
	}

}
