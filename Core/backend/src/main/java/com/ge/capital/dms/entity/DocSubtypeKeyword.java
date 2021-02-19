package com.ge.capital.dms.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "docusign_keyword")
public class DocSubtypeKeyword {
	@Id
	private String keyword;
	private String doc_subtype;
	private String include_welcomepkg;
	private String include_syndicationpkg;
	private String include_tiaapkg;
	private String entitytype;
	
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public String getDoc_subtype() {
		return doc_subtype;
	}
	public void setDoc_subtype(String doc_subtype) {
		this.doc_subtype = doc_subtype;
	}
	public String getInclude_welcomepkg() {
		return include_welcomepkg;
	}
	public void setInclude_welcomepkg(String include_welcomepkg) {
		this.include_welcomepkg = include_welcomepkg;
	}
	public String getInclude_syndicationpkg() {
		return include_syndicationpkg;
	}
	public void setInclude_syndicationpkg(String include_syndicationpkg) {
		this.include_syndicationpkg = include_syndicationpkg;
	}
	public String getInclude_tiaapkg() {
		return include_tiaapkg;
	}
	public void setInclude_tiaapkg(String include_tiaapkg) {
		this.include_tiaapkg = include_tiaapkg;
	}
	public String getEntitytype() {
		return entitytype;
	}
	public void setEntitytype(String entitytype) {
		this.entitytype = entitytype;
	}
	@Override
	public String toString() {
		return "DocSubtypeKeyword [keyword=" + keyword + ", doc_subtype=" + doc_subtype + ", include_welcomepkg="
				+ include_welcomepkg + ", include_syndicationpkg=" + include_syndicationpkg + ", include_tiaapkg="
				+ include_tiaapkg + ", entitytype=" + entitytype + "]";
	}	
	
}

