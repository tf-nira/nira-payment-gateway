package io.mosip.gateway.payment.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GeneratePRNResultDTO implements Serializable{/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String errorCode;
    private String errorDesc;
    private String expiryDate;
    private String prn;
    private String searchCode;
    private String amount;
}
