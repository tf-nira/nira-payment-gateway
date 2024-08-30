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
public class SignCredsResponseDTO implements Serializable{/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String signedCredentials;
	private String encryptedCredentials;
	private String error;
	

}
