package io.mosip.gateway.payment.dto.ura;

import java.io.Serializable;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.Data;

@Data
@JacksonXmlRootElement(localName ="GetPRNResult", namespace = "http://schemas.datacontract.org/2004/07/URAPaymentGateway.DataContracts")
public class URAGetPRNResultDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	@JacksonXmlProperty(localName ="ErrorCode", namespace = "http://schemas.datacontract.org/2004/07/URAPaymentGateway.DataContracts")
	private String errorCode;
	
	@JacksonXmlProperty(localName ="ErrorDesc", namespace = "http://schemas.datacontract.org/2004/07/URAPaymentGateway.DataContracts")
    private String errorDesc;
	
	@JacksonXmlProperty(localName ="ExpiryDate", namespace = "http://schemas.datacontract.org/2004/07/URAPaymentGateway.DataContracts")
    private String expiryDate;
	
	@JacksonXmlProperty(localName ="PRN", namespace = "http://schemas.datacontract.org/2004/07/URAPaymentGateway.DataContracts")
    private String prn;
	
	@JacksonXmlProperty(localName ="SearchCode", namespace = "http://schemas.datacontract.org/2004/07/URAPaymentGateway.DataContracts")
    private String searchCode;
}
