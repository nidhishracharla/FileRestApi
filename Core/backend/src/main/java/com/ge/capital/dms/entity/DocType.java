package com.ge.capital.dms.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "type_subtype_lookup")

public class DocType implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "type_id", unique = true, nullable = false)
	private int type_id;
	
	@Column(name = "type_label", unique = true, nullable = false, length = 20)
	private String type_label;
	
	@Column(name = "subtype_id", unique = false, nullable = false)
	private int subtype_id;
	
	@Column(name = "subtype_label", unique = false, nullable = false, length = 20)
	private String subtype_label;
	
	

	public int getType_id() {
		return type_id;
	}

	public void setType_id(int type_id) {
		this.type_id = type_id;
	}

	public String getType_label() {
		return type_label;
	}

	public void setType_label(String type_label) {
		this.type_label = type_label;
	}

	public int getSubtype_id() {
		return subtype_id;
	}

	public void setSubtype_id(int subtype_id) {
		this.subtype_id = subtype_id;
	}

	public String getSubtype_label() {
		return subtype_label;
	}

	public void setSubtype_label(String subtype_label) {
		this.subtype_label = subtype_label;
	}

	/*public int getType_subtype_id() {
		return type_subtype_id;
	}

	public void setType_subtype_id(int type_subtype_id) {
		this.type_subtype_id = type_subtype_id;
	}
	*/
	
	
}
