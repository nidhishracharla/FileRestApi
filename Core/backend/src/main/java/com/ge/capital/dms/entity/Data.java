package com.ge.capital.dms.entity;

import java.util.List;

public class Data {
	private String portStatus;

	private String DBStatus;
	private List poolHealth;
	
	public List getPoolHealth() {
		return poolHealth;
	}

	public void setPoolHealth(List poolHealth) {
		this.poolHealth = poolHealth;
	}

	public String getDBStatus() {
		return DBStatus;
	}

	public void setDBStatus(String dBStatus) {
		DBStatus = dBStatus;
	}

	public String getPortStatus() {
		return portStatus;
	}

	public void setPortStatus(String portStatus) {
		this.portStatus = portStatus;
	}
	
	
	

}
