package io.mosip.gateway.payment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrnPaymentStatusDTO {
	
	private String message;
	private int code;
	private CheckPRNStatusUraResultDTO data;
}
