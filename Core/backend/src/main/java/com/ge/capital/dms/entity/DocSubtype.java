package com.ge.capital.dms.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "doc_subtype")
public class DocSubtype {
	@Id
	@Column(name = "doc_subtype", unique = false, nullable = false)
	private String subtype;
	@Column(name = "is_final")
	private String isFinal;
	@Column(name = "keyword")
	private String keyword;
	@Column(name = "include_welcomepkg")
	private String includeWelcomepkg;
	@Column(name = "include_tiaapkg")
	private String includeTiaapkg;
	@Column(name = "primary_entitytype")
	private String primaryEntitytype;
	
	public String getIncludeWelcomepkg() {
		return includeWelcomepkg;
	}

	public void setIncludeWelcomepkg(String includeWelcomepkg) {
		this.includeWelcomepkg = includeWelcomepkg;
	}

	public String getIncludeTiaapkg() {
		return includeTiaapkg;
	}

	public void setIncludeTiaapkg(String includeTiaapkg) {
		this.includeTiaapkg = includeTiaapkg;
	}

	public String getPrimaryEntitytype() {
		return primaryEntitytype;
	}

	public void setPrimaryEntitytype(String primaryEntitytype) {
		this.primaryEntitytype = primaryEntitytype;
	}

	
	
	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}	
	
	public String getSubtype() {
		return subtype;
	}

	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}

	public String getIsFinal() {
		return isFinal;
	}

	public void setIsFinal(String isFinal) {
		this.isFinal = isFinal;
	}
	
	

}

