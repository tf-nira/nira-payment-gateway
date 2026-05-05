package io.mosip.gateway.payment.dto.ura;

import java.io.Serializable;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Data;

@Data
public class URAGetAssessmentResultDTO implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@JacksonXmlProperty(
        localName = "AssessmentDataContract.NTRAssessment",
        namespace = "http://schemas.datacontract.org/2004/07/URAPaymentGateway.DataContracts"
    )
    private URANTRAssessmentDTO assessment;
}
