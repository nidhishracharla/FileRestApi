package com.ge.capital.dms.exception;



public class CustomGenericException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int errorCode;
	private String erorrMessage;

	public CustomGenericException(int errorCode, String erorrMessage) {
		this.errorCode = errorCode;
		this.erorrMessage = erorrMessage;
	}

	public int getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	public String getErorrMessage() {
		return erorrMessage;
	}
	public void setErorrMessage(String erorrMessage) {
		this.erorrMessage = erorrMessage;
	}
}

