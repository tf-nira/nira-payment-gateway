package io.mosip.gateway.payment.constants;

public enum AppSuccessMessages {

	PRN_STATUS_CHECK_SUCCESS(AppConstants.STAGE_CHECK_PRN_STATUS + " - PRN Status Check successful"),
	PRN_CONSUMPTION_SUCCESS(AppConstants.STAGE_CONSUME_PRN_AS_USED + " - PRN Consumption successful"),
	PRN_GENERATION_SUCCESS(AppConstants.STAGE_GENERATE_PRN + " - PRN Generation successful"),
	SOAP_REQUEST_SUCCESS(AppConstants.MAIN_APP + "SOAP request successful"),
	SOAP_RESPONSE_SUCCESS(AppConstants.MAIN_APP + "SOAP response successful");
	
	private final String successMessage; 

	private AppSuccessMessages(String successMsg) {
		this.successMessage = successMsg;
	}

	public String getMessage() {
		return this.successMessage;
	}
}
