package io.mosip.gateway.payment.dto;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PrnConsumedBooleanDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private boolean prnAlreadyUsed;

}
