package io.mosip.gateway.payment.dto.response;

import java.io.Serializable;

import lombok.Data;

@Data
public class IsPRNRegInLogsResponseDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String prn;
	private String regIdTagged;
	private boolean isPresentInLogs;

}
