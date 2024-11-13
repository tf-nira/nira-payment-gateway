package io.mosip.gateway.payment.dto.ura;

import java.io.Serializable;

import lombok.Data;

@Data
public class URASoapCheckPRNStatusRequestDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String strPRN;
    private String concatenatedUsernamePasswordSignature;
    private String encryptedConcatenatedUsernamePassword;
    private String userName;
}
