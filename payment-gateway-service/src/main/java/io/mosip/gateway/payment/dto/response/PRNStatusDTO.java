package io.mosip.gateway.payment.dto.response;

import io.mosip.gateway.payment.dto.request.CheckPRNStatusResultDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PRNStatusDTO {
	
	private String message;
	private String code;
	private CheckPRNStatusResultDTO data;
}
