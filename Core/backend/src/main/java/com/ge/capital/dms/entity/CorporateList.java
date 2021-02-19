package com.ge.capital.dms.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "TB_CORPORATE_LIST")

public class CorporateList implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	@Id
	@Column(name = "corp_val", unique = true, nullable = false, length = 5)
	private String corpVal;
	@Column(name = "corp_name", unique = false, nullable = false, length = 75)
	private String corpName;
	@Column(name = "corp_label", unique = false, nullable = false, length = 75)
	private String corpLabel;
	public String getCorpVal() {
		return corpVal;
	}
	public void setCorpVal(String corpVal) {
		this.corpVal = corpVal;
	}
	public String getCorpName() {
		return corpName;
	}
	public void setCorpName(String corpName) {
		this.corpName = corpName;
	}
	public String getCorpLabel() {
		return corpLabel;
	}
	public void setCorpLabel(String corpLabel) {
		this.corpLabel = corpLabel;
	}

}
