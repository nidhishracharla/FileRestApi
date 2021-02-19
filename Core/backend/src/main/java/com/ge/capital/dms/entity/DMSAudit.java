package com.ge.capital.dms.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "dms_audit")
public class DMSAudit implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "doc_id")
	private String docId;
	
	@Column(name = "event")
	private String event;
	
	@Column(name = "doc_type")
	private String docType;

	@Column(name = "performer")
	private String performer;
	
	@Column(name = "event_time")
	private Timestamp eventTime;
	
	@Column(name = "event_details")
	private String eventDetails;
	
	@Column(name = "event_status")
	private String eventStatus;
	
	public String getDocId() {
		return docId;
	}

	public void setDocId(String docId) {
		this.docId = docId;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getDocType() {
		return docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}

	public String getPerformer() {
		return performer;
	}

	public void setPerformer(String performer) {
		this.performer = performer;
	}

	public Timestamp getEventTime() {
		return (Timestamp)eventTime.clone();
	}

	public void setEventTime(Timestamp eventTime) {
		this.eventTime = (Timestamp)eventTime.clone();
	}

	public String getEventDetails() {
		return eventDetails;
	}

	public void setEventDetails(String eventDetails) {
		this.eventDetails = eventDetails;
	}

	public String getEventStatus() {
		return eventStatus;
	}

	public void setEventStatus(String eventStatus) {
		this.eventStatus = eventStatus;
	}
}
