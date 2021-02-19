package com.ge.capital.dms.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "invoiceDocument")
public class InvoiceDocument implements Serializable {

	private static final long serialVersionUID = 2L;

	@Id
	@JoinColumn(name = "invoiceDocId")
	private String invoiceDocId;
	private String billingId;
	private String invoiceNumber;
	private String transactionIdm;
	private Timestamp invoiceDate;
	private String customerName;
	private Timestamp fileDate;
	private Timestamp dueDate;
	private String customerNumber;
	private Timestamp invoiceDueDate;
	private String invoiceType;
	private String accountNumber;
	private Timestamp interestYear;
	private String fileName;
	private String folder;
	/*private String lwGuid;
	private String receivableAmount;
	private String taxAmount;
	private Float totalAmount;
	private String currency;
	private Timestamp generatedDate;
	private String receivableCategory;
	private String isActive;*/

	public String getInvoiceDocId() {
		return invoiceDocId;
	}

	public void setInvoiceDocId(String invoiceDocId) {
		this.invoiceDocId = invoiceDocId;
	}

	public String getBillingId() {
		return billingId;
	}

	public void setBillingId(String billingId) {
		this.billingId = billingId;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public String getTransactionIdm() {
		return transactionIdm;
	}

	public void setTransactionIdm(String transactionIdm) {
		this.transactionIdm = transactionIdm;
	}

	public Timestamp getInvoiceDate() {
		return (Timestamp) invoiceDate.clone();
	}

	public void setInvoiceDate(Timestamp invoiceDate) {
		this.invoiceDate = (Timestamp) invoiceDate.clone();
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public Timestamp getFileDate() {
		return (Timestamp) fileDate.clone();
	}

	public void setFileDate(Timestamp fileDate) {
		this.fileDate = (Timestamp) fileDate.clone();
	}

	public Timestamp getDueDate() {
		return (Timestamp) dueDate.clone();
	}

	public void setDueDate(Timestamp dueDate) {
		this.dueDate = (Timestamp) dueDate.clone();
	}

	public String getCustomerNumber() {
		return customerNumber;
	}

	public void setCustomerNumber(String customerNumber) {
		this.customerNumber = customerNumber;
	}

	public Timestamp getInvoiceDueDate() {
		return (Timestamp) invoiceDueDate.clone();
	}

	public void setInvoiceDueDate(Timestamp invoiceDueDate) {
		this.invoiceDueDate = (Timestamp) invoiceDueDate.clone();
	}

	public String getInvoiceType() {
		return invoiceType;
	}

	public void setInvoiceType(String invoiceType) {
		this.invoiceType = invoiceType;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public Timestamp getInterestYear() {
		return (Timestamp) interestYear.clone();
	}

	public void setInterestYear(Timestamp interestYear) {
		this.interestYear = (Timestamp) interestYear.clone();
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	/*public String getLwnetGuid() {
		return lwGuid;
	}

	public void setLwGuid(String lwGuid) {
		this.lwGuid = lwGuid;
	}

	public String getReceivableAmount() {
		return receivableAmount;
	}

	public void setReceivableAmount(String receivableAmount) {
		this.receivableAmount = receivableAmount;
	}

	public String getTaxAmount() {
		return taxAmount;
	}

	public void setTaxAmount(String taxAmount) {
		this.taxAmount = taxAmount;
	}

	public Float getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Float totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public void setGeneratedDate(Timestamp generatedDate) {
		this.generatedDate = (Timestamp) generatedDate.clone();
	}

	public Timestamp getGeneratedDate() {
		return (Timestamp) generatedDate.clone();
	}
	

	public String getReceivableCategory() {
		return receivableCategory;
	}

	public void setReceivableCategory(String receivableCategory) {
		this.receivableCategory = receivableCategory;
	}

	public String getIsActive() {
		return isActive;
	}

	public void setIsActive(String isActive) {
		this.isActive = isActive;
	}*/

}
