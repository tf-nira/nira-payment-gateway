package io.mosip.gateway.payment.dto.request;

import java.io.Serializable;

import lombok.Data;

@Data
public class IsPRNRegInLogsRequestDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String prn;
	//private String regId;

}
