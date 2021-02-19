package com.ge.capital.dms.entity;

import java.math.BigInteger;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "doc_metadata")
public class DocMetadata {

	@Id
	private BigInteger id;
	private String docMetaKey;
	private String docMetaVal;
	
	@ManyToOne
	@JoinColumn(name = "docId")
	private CommonDoc commonDoc;

	public CommonDoc getCommonDoc() {
		return commonDoc;
	}

	public void setCommonDoc(CommonDoc commonDoc) {
		this.commonDoc = commonDoc;
	}

	public BigInteger getId() {
		return id;
	}

	public void setId(BigInteger id) {
		this.id = id;
	}

	public String getDocMetaKey() {
		return docMetaKey;
	}

	public void setDocMetaKey(String docMetaKey) {
		this.docMetaKey = docMetaKey;
	}

	public String getDocMetaVal() {
		return docMetaVal;
	}

	public void setDocMetaVal(String docMetaVal) {
		this.docMetaVal = docMetaVal;
	}

	

}
