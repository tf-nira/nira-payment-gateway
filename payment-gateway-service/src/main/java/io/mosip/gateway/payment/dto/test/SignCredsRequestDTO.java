package io.mosip.gateway.payment.dto.test;

import java.io.Serializable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class SignCredsRequestDTO implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String userName;
	private String password;

}
