package io.mosip.gateway.payment.dto.ura;

import java.io.Serializable;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "GetPRN_Foreign_CurrencyResponse", namespace = "http://tempuri.org/")
public class URASoapGetPRNForeignCurrencyResponseDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@JacksonXmlProperty(localName = "GetPRN_Foreign_CurrencyResult", namespace = "http://schemas.datacontract.org/2004/07/URAPaymentGateway.DataContracts")
	private URAGetPRNForeignCurrencyResultDTO getPRNResult;
}
