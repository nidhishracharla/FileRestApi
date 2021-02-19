package com.ge.capital.dms.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "cash_archival_doc")
public class CashArchivalDoc implements Serializable {

	private static final long serialVersionUID = 2L;

	@Id
	@JoinColumn(name = "doc_id")
	private String doc_id;
	private Timestamp date_loaded;
	private String cash_archival_type;
	
	public Timestamp getDate_loaded() {
		return  (Timestamp) date_loaded.clone();
	}
	public void setDate_loaded(Timestamp date_loaded) {
		this.date_loaded = (Timestamp)  date_loaded.clone();
	}
	public String getCash_archival_type() {
		return cash_archival_type;
	}
	public void setCash_archival_type(String cash_archival_type) {
		this.cash_archival_type = cash_archival_type;
	}
	public String getDoc_id() {
		return doc_id;
	}
	public void setDoc_id(String doc_id) {
		this.doc_id = doc_id;
	}

}
