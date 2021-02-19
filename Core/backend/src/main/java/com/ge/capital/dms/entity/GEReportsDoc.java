package com.ge.capital.dms.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "gereport_doc")
public class GEReportsDoc implements Serializable {

	private static final long serialVersionUID = 2L;

	@Id
	@JoinColumn(name = "reports_Doc_Id")
	private String reports_Doc_Id;
	private String report_id;
	private Timestamp report_date;
	private Timestamp report_run_date;
	private String report_job_no;

	public String getReports_Doc_Id() {
		return reports_Doc_Id;
	}

	public void setReports_Doc_Id(String reports_Doc_Id) {
		this.reports_Doc_Id = reports_Doc_Id;
	}

	public String getReport_id() {
		return report_id;
	}

	public void setReport_id(String report_id) {
		this.report_id = report_id;
	}

	public Timestamp getReport_date() {
		return (Timestamp) report_date.clone();
	}

	public void setReport_date(Timestamp report_date) {
		this.report_date = (Timestamp) report_date.clone();
	}

	public Timestamp getReport_run_date() {
		return (Timestamp) report_run_date.clone();
	}

	public void setReport_run_date(Timestamp report_run_date) {
		this.report_run_date = (Timestamp) report_run_date.clone();
	}

	public String getReport_job_no() {
		return report_job_no;
	}

	public void setReport_job_no(String report_job_no) {
		this.report_job_no = report_job_no;
	}

}
