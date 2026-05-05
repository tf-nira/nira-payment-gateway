package io.mosip.gateway.payment.dto.ura;

import java.io.Serializable;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "GetAssessmentAmountResponse", namespace = "http://tempuri.org/")
public class URASoapGetAssessmentResponseDTO implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@JacksonXmlProperty(
        localName = "GetAssessmentAmountResult",
        namespace = "http://tempuri.org/"
    )
    private URAGetAssessmentResultDTO result;
}
