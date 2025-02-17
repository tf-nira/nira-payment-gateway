package io.mosip.gateway.payment.constants;

public enum AppLogMessages {

	PRN_STATUS_CHECK_PRN_IN_DB_VALID("PRN found in db and valid"),
	PRN_STATUS_CHECK_PRN_IN_DB_NOT_VALID("PRN found in db but not valid so contacting URA"),
	PRN_STATUS_CHECK_PRN_NOT_IN_DB("PRN not found in db so contacting URA");
	
	private final String message; 

	private AppLogMessages(String msg) {
		this.message = msg;
	}

	public String getMessage() {
		return this.message;
	}

}
