package com.ge.capital.dms.entity;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "corptaxDoc")
public class CorptaxDoc implements Serializable {

	private static final long serialVersionUID = 3L;

	@Id
	@JoinColumn(name = "corptaxDocId")
	private String corptaxDocId;
	private String ctAmount;
	private String ctCheckRequestNum;
	private String ctCheckNum;
	private String ctYear;
	private String ctDocumentName;
	private Timestamp ctScanDate;
	private String ctJurisdiction;
	private String ctCorporation;
	private String ctCountyName;
	private String ctCountyCode;
	private String ctState;

	public String getCorptaxDocId() {
		return corptaxDocId;
	}

	public void setCorptaxDocId(String corptaxDocId) {
		this.corptaxDocId = corptaxDocId;
	}

	public String getCtAmount() {
		return ctAmount;
	}

	public void setCtAmount(String ctAmount) {
		this.ctAmount = ctAmount;
	}

	public String getCtCheckRequestNum() {
		return ctCheckRequestNum;
	}

	public void setCtCheckRequestNum(String ctCheckRequestNum) {
		this.ctCheckRequestNum = ctCheckRequestNum;
	}

	public String getCtCheckNum() {
		return ctCheckNum;
	}

	public void setCtCheckNum(String ctCheckNum) {
		this.ctCheckNum = ctCheckNum;
	}

	public String getCtYear() {
		return ctYear;
	}

	public void setCtYear(String ctYear) {
		this.ctYear = ctYear;
	}

	public String getCtDocumentName() {
		return ctDocumentName;
	}

	public void setCtDocumentName(String ctDocumentName) {
		this.ctDocumentName = ctDocumentName;
	}

	public Date getCtScanDate() {
		return (Date) this.ctScanDate.clone();
	}

	public void setCtScanDate(Timestamp ctScanDate) {
		this.ctScanDate = (Timestamp) ctScanDate.clone();
	}

	public String getCtJurisdiction() {
		return ctJurisdiction;
	}

	public void setCtJurisdiction(String ctJurisdiction) {
		this.ctJurisdiction = ctJurisdiction;
	}

	public String getCtCorporation() {
		return ctCorporation;
	}

	public void setCtCorporation(String ctCorporation) {
		this.ctCorporation = ctCorporation;
	}

	public String getCtCountyName() {
		return ctCountyName;
	}

	public void setCtCountyName(String ctCountyName) {
		this.ctCountyName = ctCountyName;
	}

	public String getCtCountyCode() {
		return ctCountyCode;
	}

	public void setCtCountyCode(String ctCountyCode) {
		this.ctCountyCode = ctCountyCode;
	}

	public String getCtState() {
		return ctState;
	}

	public void setCtState(String ctState) {
		this.ctState = ctState;
	}

}
