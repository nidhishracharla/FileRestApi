package com.ge.capital.dms.entity;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "locbox_doc")
public class Lockbox implements Serializable {

	private static final long serialVersionUID = 2L;

	@Id
	@JoinColumn(name = "lockboxDocId")
	private String lockboxDocId;
	private String aba_number;
	private String batch_item;
	private String batch_number;
	private String image_type;
	private Float check_amount;
	private Timestamp deposit_date;
	private String invoice_number;
	private String total_amount;
	private String check_number;
	private String current_amount;
	private String lockbox_number;
	private String check_last8;
	private String acc_schedule;
	private String checking_acc_num;
	private String lockbox_trans_uid;
	private String lockbox_tid;
	private Timestamp date_loaded;
	private String filenet_guid;
	private String doc_title;
	private String billing_idm;
	private String doc_class;
	
	public String getLockboxDocId() {
		return lockboxDocId;
	}

	public void setLockboxDocId(String lockboxDocId) {
		this.lockboxDocId = lockboxDocId;
	}

	public String getAba_number() {
		return aba_number;
	}

	public void setAba_number(String aba_number) {
		this.aba_number = aba_number;
	}

	public String getBatch_item() {
		return batch_item;
	}

	public void setBatch_item(String batch_item) {
		this.batch_item = batch_item;
	}

	public String getBatch_number() {
		return batch_number;
	}

	public void setBatch_number(String batch_number) {
		this.batch_number = batch_number;
	}

	public String getImage_type() {
		return image_type;
	}

	public void setImage_type(String image_type) {
		this.image_type = image_type;
	}

	public Float getCheck_amount() {
		return check_amount;
	}

	public void setCheck_amount(Float check_amount) {
		this.check_amount = check_amount;
	}

	public Timestamp getDeposit_date() {
		return (Timestamp) deposit_date.clone();
	}

	public void setDeposit_date(Timestamp deposit_date) {
		this.deposit_date = (Timestamp) deposit_date.clone();
	}

	public String getInvoice_number() {
		return invoice_number;
	}

	public void setInvoice_number(String invoice_number) {
		this.invoice_number = invoice_number;
	}

	public String getTotal_amount() {
		return total_amount;
	}

	public void setTotal_amount(String total_amount) {
		this.total_amount = total_amount;
	}

	public String getCheck_number() {
		return check_number;
	}

	public void setCheck_number(String check_number) {
		this.check_number = check_number;
	}

	public String getCurrent_amount() {
		return current_amount;
	}

	public void setCurrent_amount(String current_amount) {
		this.current_amount = current_amount;
	}

	public String getLockbox_number() {
		return lockbox_number;
	}

	public void setLockbox_number(String lockbox_number) {
		this.lockbox_number = lockbox_number;
	}

	public String getCheck_last8() {
		return check_last8;
	}

	public void setCheck_last8(String check_last8) {
		this.check_last8 = check_last8;
	}

	public String getAcc_schedule() {
		return acc_schedule;
	}

	public void setAcc_schedule(String acc_schedule) {
		this.acc_schedule = acc_schedule;
	}

	public String getChecking_acc_num() {
		return checking_acc_num;
	}

	public void setChecking_acc_num(String checking_acc_num) {
		this.checking_acc_num = checking_acc_num;
	}

	public String getLockbox_trans_uid() {
		return lockbox_trans_uid;
	}

	public void setLockbox_trans_uid(String lockbox_trans_uid) {
		this.lockbox_trans_uid = lockbox_trans_uid;
	}

	public String getLockbox_tid() {
		return lockbox_tid;
	}

	public void setLockbox_tid(String lockbox_tid) {
		this.lockbox_tid = lockbox_tid;
	}

	public Date getDate_loaded() {
		return (Date) date_loaded.clone();
	}

	public void setDate_loaded(Timestamp date_loaded) {
		this.date_loaded = (Timestamp) date_loaded.clone();
	}

	public String getFilenet_guid() {
		return filenet_guid;
	}

	public void setFilenet_guid(String filenet_guid) {
		this.filenet_guid = filenet_guid;
	}

	public String getDoc_title() {
		return doc_title;
	}

	public void setDoc_title(String doc_title) {
		this.doc_title = doc_title;
	}

	public String getBilling_idm() {
		return billing_idm;
	}

	public void setBilling_idm(String billing_idm) {
		this.billing_idm = billing_idm;
	}

	public String getDoc_class() {
		return doc_class;
	}

	public void setDoc_class(String doc_class) {
		this.doc_class = doc_class;
	}

}
