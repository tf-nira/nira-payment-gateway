package io.mosip.gateway.payment.dto;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CheckPRNStatusResponseDTO {

	private String message;
	private String code;
	private Object data;
}
