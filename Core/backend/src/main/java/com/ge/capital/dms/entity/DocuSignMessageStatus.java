package com.ge.capital.dms.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "docusign")
public class DocuSignMessageStatus {
	
//	@Id
//	@GeneratedValue(strategy = GenerationType.AUTO)
//	@Column(name = "sno")
//	private int sno;
	
	@Id
	@Column(name = "doc_id")
	private String docId;	
	@Column(name = "envelope_id")
	private String envelopeId;
	@Column(name = "docsign_filename")
	private String docusignFilename;
	@Column(name = "docusign_status")
	private String docusignStatus;
	@Column(name = "error_message")
	private String errorMessage;
	@Column(name = "created_by")
	private String createdBy;
	@Column(name = "created_date")
	private String createdDate;
	@Column(name = "modified_by")
	private String modifiedBy;
	@Column(name = "modified_date")
	private String modifiedDate;
	@Column(name = "legalentitytype")
	private String legalentitytype;
	
//	public int getSno() {
//		return sno;
//	}
//	public void setSno(int sno) {
//		this.sno = sno;
//	}
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	public String getEnvelopeId() {
		return envelopeId;
	}
	public void setEnvelopeId(String envelopeId) {
		this.envelopeId = envelopeId;
	}
	public String getDocusignFilename() {
		return docusignFilename;
	}
	public void setDocusignFilename(String docusignFilename) {
		this.docusignFilename = docusignFilename;
	}
	public String getDocusignStatus() {
		return docusignStatus;
	}
	public void setDocusignStatus(String docusignStatus) {
		this.docusignStatus = docusignStatus;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public String getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}
	public String getModifiedBy() {
		return modifiedBy;
	}
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	public String getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(String modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	public String getLegalentitytype() {
		return legalentitytype;
	}
	public void setLegalentitytype(String legalentitytype) {
		this.legalentitytype = legalentitytype;
	}
	@Override
	public String toString() {
		return "DocuSignMessageStatus [docId=" + docId + ", envelopeId=" + envelopeId
				+ ", docusignFilename=" + docusignFilename + ", docusignStatus=" + docusignStatus + ", errorMessage="
				+ errorMessage + ", createdBy=" + createdBy + ", createdDate=" + createdDate + ", modifiedBy="
				+ modifiedBy + ", modifiedDate=" + modifiedDate + ", legalentitytype=" + legalentitytype + "]";
	}
	

}
