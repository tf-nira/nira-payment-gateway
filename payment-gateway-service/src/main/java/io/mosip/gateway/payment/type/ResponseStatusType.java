package io.mosip.gateway.payment.type;

public enum ResponseStatusType {

	CREATED("CREATED"),
    UPDATED("UPDATED"),
    DELETED("DELETED"),
    ACTIVATED("ACTIVATED"),
    DEACTIVATED("DEACTIVATED"),
    FAILED("FAILED");

	private final String statusType;
	
	private ResponseStatusType(String statusType) {
		this.statusType = statusType;

	}

	public String getStatusType() {
		return statusType;
	}
}
