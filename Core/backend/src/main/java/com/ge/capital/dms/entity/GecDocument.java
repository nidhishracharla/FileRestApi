package com.ge.capital.dms.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "gec_document")
public class GecDocument implements Serializable {

	private static final long serialVersionUID = 2L;

	@Id
	@JoinColumn(name = "gecDocId")
	private String gecDocId;
	private String category_id;
	private String type_id;
	
	@JoinColumn(name = "subtype_id")
	private String subtype_id;
	private String quote_id;
	private String class_id;
	private String customer_id;
	private String contract_id;
	private String sum_id;
	private String transaction_id;
	private String credit_id;
	private String gec_dockey;
	private String takedown_idm;
	private String submittal_idm;
	private String platform;
	private String ref_cust_id;
	private String customer_elig;
	private String program;
	private String envelope_id;
	private String reference_id;
	private String sr_number;
	private String hold;
	private String doc_date;
	private String program_segment;
	private String scan_date;
	private String vin_no;

	public String getVin_no() {
		return vin_no;
	}

	public void setVin_no(String vin_no) {
		this.vin_no = vin_no;
	}

	public String getGecDocId() {
		return gecDocId;
	}

	public void setGecDocId(String gecDocId) {
		this.gecDocId = gecDocId;
	}

	public String getCategory_id() {
		return category_id;
	}

	public void setCategory_id(String category_id) {
		this.category_id = category_id;
	}

	public String getType_id() {
		return type_id;
	}

	public void setType_id(String type_id) {
		this.type_id = type_id;
	}

	public String getSubtype_id() {
		return subtype_id;
	}

	public void setSubtype_id(String subtype_id) {
		this.subtype_id = subtype_id;
	}

	public String getQuote_id() {
		return quote_id;
	}

	public void setQuote_id(String quote_id) {
		this.quote_id = quote_id;
	}

	public String getClass_id() {
		return class_id;
	}

	public void setClass_id(String class_id) {
		this.class_id = class_id;
	}

	public String getCustomer_id() {
		return customer_id;
	}

	public void setCustomer_id(String customer_id) {
		this.customer_id = customer_id;
	}

	public String getContract_id() {
		return contract_id;
	}

	public void setContract_id(String contract_id) {
		this.contract_id = contract_id;
	}

	public String getSum_id() {
		return sum_id;
	}

	public void setSum_id(String sum_id) {
		this.sum_id = sum_id;
	}

	public String getCredit_id() {
		return credit_id;
	}

	public void setCredit_id(String credit_id) {
		this.credit_id = credit_id;
	}

	public String getTransaction_id() {
		return transaction_id;
	}

	public void setTransaction_id(String transaction_id) {
		this.transaction_id = transaction_id;
	}

	public String getGec_dockey() {
		return gec_dockey;
	}

	public void setGec_dockey(String gec_dockey) {
		this.gec_dockey = gec_dockey;
	}

	public String getTakedown_idm() {
		return takedown_idm;
	}

	public void setTakedown_idm(String takedown_idm) {
		this.takedown_idm = takedown_idm;
	}

	public String getSubmittal_idm() {
		return submittal_idm;
	}

	public void setSubmittal_idm(String submittal_idm) {
		this.submittal_idm = submittal_idm;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getRef_cust_id() {
		return ref_cust_id;
	}

	public void setRef_cust_id(String ref_cust_id) {
		this.ref_cust_id = ref_cust_id;
	}

	public String getCustomer_elig() {
		return customer_elig;
	}

	public void setCustomer_elig(String customer_elig) {
		this.customer_elig = customer_elig;
	}

	public String getProgram() {
		return program;
	}

	public void setProgram(String program) {
		this.program = program;
	}

	public String getEnvelope_id() {
		return envelope_id;
	}

	public void setEnvelope_id(String envelope_id) {
		this.envelope_id = envelope_id;
	}

	public String getReference_id() {
		return reference_id;
	}

	public void setReference_id(String reference_id) {
		this.reference_id = reference_id;
	}

	public String getSr_number() {
		return sr_number;
	}

	public void setSr_number(String sr_number) {
		this.sr_number = sr_number;
	}

	public String getHold() {
		return hold;
	}

	public void setHold(String hold) {
		this.hold = hold;
	}

	public String getDoc_date() {
		return doc_date;
	}

	public void setDoc_date(String doc_date) {
		this.doc_date = doc_date;
	}

	public String getProgram_segment() {
		return program_segment;
	}

	public void setProgram_segment(String program_segment) {
		this.program_segment = program_segment;
	}

	public String getScan_date() {
		return scan_date;
	}

	public void setScan_date(String scan_date) {
		this.scan_date = scan_date;
	}
}
