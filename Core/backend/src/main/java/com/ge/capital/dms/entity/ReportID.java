package com.ge.capital.dms.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "gereport_id_list")
public class ReportID {
	
	@Id
	@Column(name = "report_id", unique = true, nullable = false)
	private String report_id;
	
	@Column(name = "report_label", unique = false, nullable = false)
	private String report_label;
	

	public String getReport_id() {
		return report_id;
	}
	public void setReport_id(String report_id) {
		this.report_id = report_id;
	}
	public String getReport_label() {
		return report_label;
	}
	public void setReport_label(String report_label) {
		this.report_label = report_label;
	}
	
}
