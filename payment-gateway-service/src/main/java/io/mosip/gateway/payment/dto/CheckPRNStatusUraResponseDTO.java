package io.mosip.gateway.payment.dto;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CheckPRNStatusUraResponseDTO {

	private String message;
	private int code;
	private Object data;
}
