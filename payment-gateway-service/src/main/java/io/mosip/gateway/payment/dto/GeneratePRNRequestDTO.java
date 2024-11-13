package io.mosip.gateway.payment.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class GeneratePRNRequestDTO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@JsonProperty("service")
	private String service;
	
	@JsonProperty("NIN")
	private String nin;
	
	@JsonProperty("fullName")
	private String fullName;

}
