package io.mosip.gateway.payment.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CheckPRNStatusResultDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String amountPaid;
    private String countyName;
    private String currency;
    private String datePaid;
    private String districtName;
    private String errorCode;
    private String errorDesc;
    private String feesPerUnit;
    private String instrumentID;
    private String landlineNumber;
    private String mdaName;
    private String maxUnit;
    private String minUnit;
    private String mobileNumber;
    private String noOfUnits;
    private String prn;
    private String paymentBank;
    private String paymentExpiryDate;
    private String paymentMode;
    private String realizationDate;
    private String referenceNumber;
    private String searchCode;
    private String stationName;
    private String statusCode;
    private String statusDesc;
    private String subcountyName;
    private String tin;
    private String taxHeadCode;
    private String taxHeadName;
    private String taxPayerEmail;
    private String taxPayerName;
    private String villageName;
	//private String processFlow; // Helper field to verifying payments from MOSIP modules
}
