package com.ge.capital.dms.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "cashreport_emailarchive")
public class CashWireEmailArchive implements Serializable {

	@Override
	public String toString() {
		return "CashWireEmailArchive [docId=" + docId + ", createDate=" + createDate + ", emailDate=" + emailDate
				+ ", emailSubject=" + emailSubject + ", emailId=" + emailId + ", invoiceNumber=" + invoiceNumber
				+ ", sequenceNumber=" + sequenceNumber + ", attachment=" + attachment + ", amount=" + amount
				+ ", creator=" + creator + ", docName=" + docName + "]";
	}

	private static final long serialVersionUID = 1L;

	// @JoinColumn(name = "doc_id")

	@Id
	private String docId;
	private String createDate;
	private String emailDate;
	private String emailSubject;
	private String emailId;
	private String invoiceNumber;
	private String sequenceNumber;
	private String attachment;
	private String amount;
	private String creator;
	private String docName;

	public String getDocId() {
		return docId;
	}

	public void setDocId(String docId) {
		this.docId = docId;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getEmailDate() {
		return emailDate;
	}

	public void setEmailDate(String emailDate) {
		this.emailDate = emailDate;
	}

	public String getEmailSubject() {
		return emailSubject;
	}

	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public String getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(String sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public String getAttachment() {
		return attachment;
	}

	public void setAttachment(String attachment) {
		this.attachment = attachment;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getDocName() {
		return docName;
	}

	public void setDocName(String docName) {
		this.docName = docName;
	}
}
