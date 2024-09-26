package io.mosip.gateway.payment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PRNGeneratedDTO {

	private String message;
	private String code;
	private GeneratePRNResultDTO data;
}
