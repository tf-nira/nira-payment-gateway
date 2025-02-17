package io.mosip.gateway.payment.dto.response;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PRNConsumedBooleanDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private boolean prnAlreadyUsed;

}
