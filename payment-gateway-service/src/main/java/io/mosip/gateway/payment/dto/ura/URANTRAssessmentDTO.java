package io.mosip.gateway.payment.dto.ura;

import java.io.Serializable;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Data;

@Data
public class URANTRAssessmentDTO implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@JacksonXmlProperty(localName = "NTRAmount")
    private Integer ntrAmount;
	
	@JacksonXmlProperty(localName = "NTRTaxHead")
    private String ntrTaxHead;
	
	@JacksonXmlProperty(localName = "NTRTaxHeadDesc")
    private String ntrTaxHeadDesc;

    @JacksonXmlProperty(localName = "StatusCode")
    private String statusCode;

    @JacksonXmlProperty(localName = "StatusDesc")
    private String statusDesc;
}
