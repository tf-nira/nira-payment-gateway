package io.mosip.gateway.payment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PRNStatusDTO {
	
	private String message;
	private String code;
	private CheckPRNStatusResultDTO data;
}
