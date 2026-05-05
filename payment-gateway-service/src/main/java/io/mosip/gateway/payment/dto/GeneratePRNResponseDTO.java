package io.mosip.gateway.payment.dto;

import lombok.Data;

@Data
public class GeneratePRNResponseDTO  {
	
	private String code;
	private String message;
	private Object data;

}
