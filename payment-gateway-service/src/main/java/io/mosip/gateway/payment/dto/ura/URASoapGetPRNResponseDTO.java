package io.mosip.gateway.payment.dto.ura;

import java.io.Serializable;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "GetPRNResponse", namespace = "http://tempuri.org/")
public class URASoapGetPRNResponseDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@JacksonXmlProperty(localName = "GetPRNResult", namespace = "http://schemas.datacontract.org/2004/07/URAPaymentGateway.DataContracts")
	private URAGetPRNResultDTO getPRNResult;

}
