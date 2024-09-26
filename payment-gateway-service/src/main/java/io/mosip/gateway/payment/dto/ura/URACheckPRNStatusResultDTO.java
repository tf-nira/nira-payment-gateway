package io.mosip.gateway.payment.dto.ura;


import java.io.Serializable;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.Data;

@Data
@JacksonXmlRootElement(localName ="CheckPRNStatusResult", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
public class URACheckPRNStatusResultDTO implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@JacksonXmlProperty(localName ="AmountPaid", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
	private String amountPaid;
	
	@JacksonXmlProperty(localName ="CountyName", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models" )
    private String countyName;
    
	@JacksonXmlProperty(localName ="Currency", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String currency;
    
	@JacksonXmlProperty(localName ="DatePaid", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String datePaid;
    
	@JacksonXmlProperty(localName ="DistrictName", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String districtName;
    
	@JacksonXmlProperty(localName ="ErrorCode", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String errorCode;
    
	@JacksonXmlProperty(localName ="ErrorDesc", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String errorDesc;
    
	@JacksonXmlProperty(localName ="FeesPerUnit", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String feesPerUnit;
    
	@JacksonXmlProperty(localName ="InstrumentID", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String instrumentID;
	
	@JacksonXmlProperty(localName ="LandlineNumber", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String landlineNumber;
    
	@JacksonXmlProperty(localName ="MDAName", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String mdaName;
    
	@JacksonXmlProperty(localName ="MaxUnit", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String maxUnit;
    
	@JacksonXmlProperty(localName ="MinUnit", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String minUnit;
    
	@JacksonXmlProperty(localName ="MobileNumber", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String mobileNumber;
    
	@JacksonXmlProperty(localName ="NoOfUnits", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String noOfUnits;
    
	@JacksonXmlProperty(localName ="PRN", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String prn;
    
	@JacksonXmlProperty(localName ="PaymentBank", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String paymentBank;
    
	@JacksonXmlProperty(localName ="PaymentExpiryDate", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String paymentExpiryDate;
    
	@JacksonXmlProperty(localName ="PaymentMode", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String paymentMode;
    
	@JacksonXmlProperty(localName ="RealizationDate", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String realizationDate;
    
	@JacksonXmlProperty(localName ="ReferenceNumber", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String referenceNumber;
    
	@JacksonXmlProperty(localName ="SearchCode", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String searchCode;
    
	@JacksonXmlProperty(localName ="StationName", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String stationName;
    
	@JacksonXmlProperty(localName ="StatusCode", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String statusCode;
    
	@JacksonXmlProperty(localName ="StatusDesc", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String statusDesc;
    
	@JacksonXmlProperty(localName ="SubcountyName", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String subcountyName;
    
	@JacksonXmlProperty(localName ="TIN", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String tin;
    
	@JacksonXmlProperty(localName ="TaxHeadCode", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String taxHeadCode;
    
	@JacksonXmlProperty(localName ="TaxHeadName", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String taxHeadName;
    
	@JacksonXmlProperty(localName ="TaxPayerEmail", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String taxPayerEmail;
    
	@JacksonXmlProperty(localName ="TaxPayerName", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String taxPayerName;
    
    @JacksonXmlProperty(localName ="VillageName", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private String villageName;
	//private String processFlow; // Helper field to verifying payments from MOSIP modules
    
}
