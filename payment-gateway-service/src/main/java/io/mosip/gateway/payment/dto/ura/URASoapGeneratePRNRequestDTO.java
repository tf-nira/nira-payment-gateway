package io.mosip.gateway.payment.dto.ura;

import java.io.Serializable;

import com.fasterxml.jackson.module.afterburner.deser.SettableStringFieldProperty;

import lombok.Data;

@Data
public class URASoapGeneratePRNRequestDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String concatenatedUsernamePasswordSignature;
	private String encryptedConcatenatedUsernamePassword;
	private String userName;
	private String taxPayerName;
	private String taxPayerNIN;
	private String taxHead;
	private Integer amount;
	private String currency;
}
