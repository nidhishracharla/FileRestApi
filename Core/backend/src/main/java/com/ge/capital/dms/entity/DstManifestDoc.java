package com.ge.capital.dms.entity;

import java.io.Serializable;
import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

//import org.joda.time.DateTime;

@Entity
@Table(name = "dst_manifest_doc")
public class DstManifestDoc implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@Id
	private String mftCreatorSearched;
	private Date mftCreationFromDtSearched;
	private Date mftCreationToDtSearched;
	private String mftSequenceNo;
	private String mftStorerNo;
	private String mftBusinessLoc;
	private String mftSenderName;	
	
	@ManyToOne
	@JoinColumn(name = "docId")
	private CommonDoc commonDoc;

	public CommonDoc getCommonDoc() {
		return commonDoc;
	}

	public void setCommonDoc(CommonDoc commonDoc) {
		this.commonDoc = commonDoc;
	}

	public String getMftCreatorSearched() {
		return mftCreatorSearched;
	}

	public void setMftCreatorSearched(String mftCreatorSearched) {
		this.mftCreatorSearched = mftCreatorSearched;
	}

	public Date getMftCreationFromDtSearched() {
		return new Date(mftCreationFromDtSearched.getTime());
	}

	public void setMftCreationFromDtSearched(Date mftCreationFromDtSearched) {
		this.mftCreationFromDtSearched = new Date(mftCreationFromDtSearched.getTime());
	}

	public Date getMftCreationToDtSearched() {
		return new Date(mftCreationToDtSearched.getTime());
	}

	public void setMftCreationToDtSearched(Date mftCreationToDtSearched) {
		this.mftCreationToDtSearched = new Date(mftCreationToDtSearched.getTime());
	}

	public String getMftSequenceNo() {
		return mftSequenceNo;
	}

	public void setMftSequenceNo(String mftSequenceNo) {
		this.mftSequenceNo = mftSequenceNo;
	}

	public String getMftStorerNo() {
		return mftStorerNo;
	}

	public void setMftStorerNo(String mftStorerNo) {
		this.mftStorerNo = mftStorerNo;
	}

	public String getMftBusinessLoc() {
		return mftBusinessLoc;
	}

	public void setMftBusinessLoc(String mftBusinessLoc) {
		this.mftBusinessLoc = mftBusinessLoc;
	}

	public String getMftSenderName() {
		return mftSenderName;
	}

	public void setMftSenderName(String mftSenderName) {
		this.mftSenderName = mftSenderName;
	}
	
}