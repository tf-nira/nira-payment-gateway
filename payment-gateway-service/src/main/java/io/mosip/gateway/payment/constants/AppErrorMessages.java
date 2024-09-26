package io.mosip.gateway.payment.constants;

public enum AppErrorMessages {

	NPG_UNKNOWN_EXCEPTION(AppConstants.MAIN_APP + "001", "Unknown Exception Found"),
	NPG_PARAM_MISSING(AppConstants.MAIN_APP + "002", "parameter missing"),
	NPG_ENTITY_NOT_FOUND(AppConstants.MAIN_APP + "003", "Entity not found in Db"),
	NPG_ENTITY_ALREADY_EXISTS(AppConstants.MAIN_APP + "004", "Entity already exists in Db"),
	NPG_REQUEST_MISSING(AppConstants.MAIN_APP + "005", "request {} missing"),
	NPG_API_REQUEST_FAILED(AppConstants.MAIN_APP + "006", "Internal Error occured while contacting URA API"),
	NPG_OBJECT_JSON_CONVERSION_FAILED(AppConstants.MAIN_APP + "007", "Failed to convert object into json {} string"),
	NPG_ENTITIES_NOT_FOUND(AppConstants.MAIN_APP + "008", "Entities not found in Db"),
	NPG_INTERNAL_ERROR(AppConstants.MAIN_APP + "009", "Internal Error occured"),
	PRN_ALREADY_CONSUMED(AppConstants.CONSUME_PRN_AS_USED + "001", "PRN was already consumed"),
	PRN_CONSUMPTION_FAILED(AppConstants.CONSUME_PRN_AS_USED + "002", "PRN consumption failed"),
	PRN_ALREADY_CONSUMED_BY_DIFFERENT_REGID(AppConstants.CONSUME_PRN_AS_USED + "002", "PRN Consumed by different Reg Id"),
	NPG_BYTE_OBJECT_CONVERSION_FAILED(AppConstants.MAIN_APP + "010", "Failed to convert bytes to object"),
	NPG_OBJECT_BYTE_CONVERSION_FAILED(AppConstants.MAIN_APP + "011", "Failed to convert object to bytes"),
	SOAP_API_REQUEST_FAILED(AppConstants.MAIN_APP + "012", "Internal Errror occured while sending SOAP Request"),
	SOAP_MESSAGE_STRING_CONVERSION_FAILED(AppConstants.MAIN_APP + "013", "Failed to convert SOAP message to string"),
	SOAP_JSON_CONVERSION_FAILED(AppConstants.MAIN_APP + "014", "Failed to convert SOAP to JSON"),
	SOAP_RESPONSE_NULL(AppConstants.MAIN_APP + "015", "SOAP Response is null"),
	SOAP_AUTHENTICATION_ERROR(AppConstants.MAIN_APP + "016", "Authentication with URA failed"),
	NPG_TAXHEAD_NOT_FOUND_FOR_SERVICE(AppConstants.MAIN_APP + "017", "Tax head not found for service"),
	PRN_GENERATION_FAILED(AppConstants.GENERATE_PRN + "001", "PRN generation failed"),
	PRN_CHECK_STATUS_FAILED(AppConstants.CHECK_PRN_STATUS + "001", "PRN status check failed"),
	URA_PRN_NOT_FOUND(AppConstants.CHECK_PRN_STATUS + "002", "No Data Found For Supplied Payment Registration Number"),
	URA_PRN_EXPIRED(AppConstants.CHECK_PRN_STATUS + "003", "PRN Expired"),
	URA_PRN_CANCELLED(AppConstants.CHECK_PRN_STATUS + "004", "PRN Cancelled"),
	URA_PRN_INVALID(AppConstants.CHECK_PRN_STATUS + "005", "PRN Invalid"),
	URA_UNEXPECTED_ERROR(AppConstants.CHECK_PRN_STATUS + "006", "Unexpected error"),
	URA_MANDATORY_FIELD_MISSING(AppConstants.GENERATE_PRN + "002", "Mandatory Field Missing / Conditional Mandatory field Missing"),
	URA_NEGATIVE_AMOUNT(AppConstants.GENERATE_PRN + "003", "Amount cannot be a negative value"),
	URA_NEGATIVE_EXPIRY_DAYS(AppConstants.GENERATE_PRN + "004", "Expiry days cannot be a negative value"),
	URA_DATA_TYPE_ERROR(AppConstants.GENERATE_PRN + "005", "Data Type Error"),
	URA_INVALID_PAYMENT_MODE(AppConstants.GENERATE_PRN + "006", "Invalid Payment Mode"),
	URA_INVALID_TAX_HEAD(AppConstants.GENERATE_PRN + "007", "Invalid Tax Head"),
	URA_INVALID_BANK_CODE(AppConstants.GENERATE_PRN + "008", "Invalid Bank Code"),;
	

	private final String errorMessage; 
	private final String errorCode;

	private AppErrorMessages(String errorCode, String errorMsg) {
		this.errorCode = errorCode;
		this.errorMessage = errorMsg;
	}

	public String getMessage() {
		return this.errorMessage;
	}

	public String getCode() {
		return this.errorCode;
	}
}

