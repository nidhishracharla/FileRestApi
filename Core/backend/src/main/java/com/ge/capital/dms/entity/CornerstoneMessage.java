package com.ge.capital.dms.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "cornerstone_message", schema = "dbo")
public class CornerstoneMessage implements Serializable {

	private static final long serialVersionUID = 4L;

	@Id
	private int attempt_count;
	private String error_cause;
	private String message_json;
	private String message_type;
	private Timestamp modified_date;
	private String status;
	private String entity_id;
	private String party_name;
	private String party_number;
	private String credit_number;

	public int getAttempt_count() {
		return attempt_count;
	}

	public void setAttempt_count(int attempt_count) {
		this.attempt_count = attempt_count;
	}

	public String getError_cause() {
		return error_cause;
	}

	public void setError_cause(String error_cause) {
		this.error_cause = error_cause;
	}

	public String getMessage_json() {
		return message_json;
	}

	public void setMessage_json(String message_json) {
		this.message_json = message_json;
	}

	public String getMessage_type() {
		return message_type;
	}

	public void setMessage_type(String message_type) {
		this.message_type = message_type;
	}

	public Timestamp getModified_date() {
		return (Timestamp) modified_date.clone();
	}

	public void setModified_date(Timestamp modified_date) {
		this.modified_date = (Timestamp) modified_date.clone();
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getEntity_id() {
		return entity_id;
	}

	public void setEntity_id(String entity_id) {
		this.entity_id = entity_id;
	}

	public String getParty_name() {
		return party_name;
	}

	public void setParty_name(String party_name) {
		this.party_name = party_name;
	}

	public String getParty_number() {
		return party_number;
	}

	public void setParty_number(String party_number) {
		this.party_number = party_number;
	}

	public String getCredit_number() {
		return credit_number;
	}

	public void setCredit_number(String credit_number) {
		this.credit_number = credit_number;
	}

}
