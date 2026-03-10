package io.mosip.gateway.payment.util;

import java.io.StringReader;
import java.io.StringWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import io.mosip.gateway.payment.dto.ura.URACheckPRNStatusResultDTO;
import io.mosip.gateway.payment.dto.ura.URAGetPRNForeignCurrencyResultDTO;
import io.mosip.gateway.payment.dto.ura.URAGetPRNResultDTO;
import io.mosip.gateway.payment.dto.ura.URASoapCheckPRNStatusRequestDTO;
import io.mosip.gateway.payment.dto.ura.URASoapCheckPRNStatusResponseDTO;
import io.mosip.gateway.payment.dto.ura.URASoapGeneratePRNRequestDTO;
import io.mosip.gateway.payment.dto.ura.URASoapGetPRNForeignCurrencyResponseDTO;
import io.mosip.gateway.payment.dto.ura.URASoapGetPRNResponseDTO;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.soap.SOAPPart;
import lombok.extern.slf4j.Slf4j;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

@Slf4j
@Component
public class URASoapServiceUtil {

	@Autowired
	private RestTemplate restTemplate;

	@Value("${ura.user}")
	private String mdaUsername;

	@Value("${ura.key}")
	private String mdaPass;

	@Value("${ura.signed}")
	private String signedCredentials;

	@Value("${ura.encrypted}")
	private String encryptedCredentials;

	@Value("${ura.wsdl}")
	private String uraWsdl;

	@Value("${ura.action.check-prn-status}")
	private String uraCheckPRNStatusSOAPAction;

	@Value("${ura.action.get-prn}")
	private String uraGetPRNSOAPAction;
	
	@Value("${ura.action.get-prn-foreign-currency}")
	private String uraGetPRNForeignCurrencyAction;
	
	@Value("${ura.taxpayer.bankcode}")
	private String uraTaxPayerBankCode;

	@Value("${ura.srcsystem}")
	private String uraSrcSystem;

	@Value("${ura.payment.mode}")
	private String uraPaymentMode;

	@Value("${ura.payment.bankcode}")
	private String uraPaymentBankCode;

	@Value("${ura.payment.type}")
	private String uraPaymentType;

	@Value("${ura.expiry-days}")
	private String uraExpiryDays;

	@Value("${ura.reference-no}")
	private String uraReferenceNo;

	@Autowired
	Jaxb2Marshaller jaxb2Marshaller;

	/**
	 * Method to make the GetPRN SOAP request.
	 */
	public String getPRN(URASoapGeneratePRNRequestDTO requestDTO) {
		String soapRequest = buildGetPRNRequest(requestDTO);
		
		//log.debug("URA SOAP Request Payload: {}", soapRequest);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_XML);
		headers.add("SOAPAction", uraGetPRNSOAPAction);

		HttpEntity<String> entity = new HttpEntity<>(soapRequest, headers);

		ResponseEntity<String> response = restTemplate.exchange(uraWsdl, HttpMethod.POST, entity, String.class);

		return response.getBody();

	}
	
	public String getPRNForeignCurrency(URASoapGeneratePRNRequestDTO requestDTO) {

	    String soapRequest = buildGetPRNForeignRequest(requestDTO);

	    //log.debug("URA Foreign Currency SOAP Request Payload: {}", soapRequest);

	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.TEXT_XML);
	    headers.add("SOAPAction", uraGetPRNForeignCurrencyAction);

	    HttpEntity<String> entity = new HttpEntity<>(soapRequest, headers);

	    ResponseEntity<String> response =
	            restTemplate.exchange(uraWsdl, HttpMethod.POST, entity, String.class);

	    return response.getBody();
	}

	/**
	 * Method to make the CheckPRNStatus SOAP request.
	 */
	public String checkPRNStatus(URASoapCheckPRNStatusRequestDTO requestDTO) {
		String soapRequest = buildCheckPRNStatusRequest(requestDTO);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_XML);
		headers.add("SOAPAction", uraCheckPRNStatusSOAPAction);

		HttpEntity<String> entity = new HttpEntity<>(soapRequest, headers);

		ResponseEntity<String> response = restTemplate.exchange(uraWsdl, HttpMethod.POST, entity, String.class);

		return response.getBody();
	}

	/**
	 * Build the GetPRN SOAP request.
	 */
//	private String buildGetPRNRequest(URASoapGeneratePRNRequestDTO requestDTO) {
//	    StringBuilder soapRequest = new StringBuilder();
//
//	    soapRequest.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ")
//	            .append("xmlns:tem=\"http://tempuri.org/\" xmlns:urap=\"http://schemas.datacontract.org/2004/07/URAPaymentGateway.DataContracts\">")
//	            .append("<soapenv:Header/>")
//	            .append("<soapenv:Body>")
//	            .append("<tem:GetPRN>")
//	            .append("<tem:PRNRequest>")
//	            .append("<urap:Amount>").append(requestDTO.getAmount()).append("</urap:Amount>")
//	            .append("<urap:ExpiryDays>21</urap:ExpiryDays>")
//	            .append("<urap:PaymentBankCode>STN</urap:PaymentBankCode>")
//	            .append("<urap:PaymentMode>CASH</urap:PaymentMode>")
//	            .append("<urap:PaymentType>DT</urap:PaymentType>")
//	            .append("<urap:ReferenceNo>").append(requestDTO.getReferenceNo()).append("</urap:ReferenceNo>")
//	            .append("<urap:SRCSystem>NIRA</urap:SRCSystem>")
//	            .append("<urap:TaxHead>").append(requestDTO.getTaxHead()).append("</urap:TaxHead>")
//	            .append("<urap:TaxPayerBankCode>STN</urap:TaxPayerBankCode>")
//	            .append("<urap:TaxPayerName>").append(requestDTO.getTaxPayerName()).append("</urap:TaxPayerName>");
//
//	    if (requestDTO.getTaxPayerNIN() != null && !requestDTO.getTaxPayerNIN().trim().isEmpty()) {
//	        soapRequest.append("<urap:TaxPayerNIN>").append(requestDTO.getTaxPayerNIN()).append("</urap:TaxPayerNIN>");
//	    }
//
//	    soapRequest.append("</tem:PRNRequest>")
//	            .append("<tem:concatenatedUsernamePasswordSignature>")
//	            .append(requestDTO.getConcatenatedUsernamePasswordSignature())
//	            .append("</tem:concatenatedUsernamePasswordSignature>")
//	            .append("<tem:encryptedConcatenatedUsernamePassword>")
//	            .append(requestDTO.getEncryptedConcatenatedUsernamePassword())
//	            .append("</tem:encryptedConcatenatedUsernamePassword>")
//	            .append("<tem:userName>").append(requestDTO.getUserName()).append("</tem:userName>")
//	            .append("</tem:GetPRN>")
//	            .append("</soapenv:Body>")
//	            .append("</soapenv:Envelope>");
//
//	    return soapRequest.toString();
//	}
	
	private String buildGetPRNRequest(URASoapGeneratePRNRequestDTO requestDTO) {

	    StringBuilder soapRequest = new StringBuilder();

	    soapRequest.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ")
	            .append("xmlns:tem=\"http://tempuri.org/\" ")
	            .append("xmlns:urap=\"http://schemas.datacontract.org/2004/07/URAPaymentGateway.DataContracts\">")
	            .append("<soapenv:Header/>")
	            .append("<soapenv:Body>")
	            .append("<tem:GetPRN>")
	            .append("<tem:PRNRequest>")

	            .append("<urap:Amount>").append(requestDTO.getAmount()).append("</urap:Amount>")
	            .append("<urap:ExpiryDays>").append(uraExpiryDays).append("</urap:ExpiryDays>")
	            .append("<urap:PaymentBankCode>").append(uraPaymentBankCode).append("</urap:PaymentBankCode>")
	            .append("<urap:PaymentMode>").append(uraPaymentMode).append("</urap:PaymentMode>")
	            .append("<urap:PaymentType>").append(uraPaymentType).append("</urap:PaymentType>")
	            .append("<urap:ReferenceNo>").append(uraReferenceNo).append("</urap:ReferenceNo>")
	            .append("<urap:SRCSystem>").append(uraSrcSystem).append("</urap:SRCSystem>")
	            .append("<urap:TaxHead>").append(requestDTO.getTaxHead()).append("</urap:TaxHead>")
	            .append("<urap:TaxPayerBankCode>").append(uraTaxPayerBankCode).append("</urap:TaxPayerBankCode>")
	            .append("<urap:TaxPayerName>").append(requestDTO.getTaxPayerName()).append("</urap:TaxPayerName>");

	    if (requestDTO.getTaxPayerNIN() != null &&
	        !requestDTO.getTaxPayerNIN().trim().isEmpty()) {

	        soapRequest.append("<urap:TaxPayerNIN>")
	                .append(requestDTO.getTaxPayerNIN())
	                .append("</urap:TaxPayerNIN>");
	    }

	    soapRequest.append("</tem:PRNRequest>")
	            .append("<tem:concatenatedUsernamePasswordSignature>")
	            .append(requestDTO.getConcatenatedUsernamePasswordSignature())
	            .append("</tem:concatenatedUsernamePasswordSignature>")
	            .append("<tem:encryptedConcatenatedUsernamePassword>")
	            .append(requestDTO.getEncryptedConcatenatedUsernamePassword())
	            .append("</tem:encryptedConcatenatedUsernamePassword>")
	            .append("<tem:userName>").append(requestDTO.getUserName()).append("</tem:userName>")
	            .append("</tem:GetPRN>")
	            .append("</soapenv:Body>")
	            .append("</soapenv:Envelope>");

	    return soapRequest.toString();
	}
	
	private String buildGetPRNForeignRequest(URASoapGeneratePRNRequestDTO requestDTO) {

	    StringBuilder soapRequest = new StringBuilder();

	    soapRequest.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ")
	            .append("xmlns:tem=\"http://tempuri.org/\" ")
	            .append("xmlns:urap=\"http://schemas.datacontract.org/2004/07/URAPaymentGateway.DataContracts\">")
	            .append("<soapenv:Header/>")
	            .append("<soapenv:Body>")
	            .append("<tem:GetPRN_Foreign_Currency>")
	            .append("<tem:PRNRequest>")

	            .append("<urap:Amount>").append(requestDTO.getAmount()).append("</urap:Amount>")
	            .append("<urap:ExpiryDays>").append(uraExpiryDays).append("</urap:ExpiryDays>")
	            .append("<urap:ForeignCurrencyCode>")
	                .append(requestDTO.getCurrency())   // dynamic currency
	            .append("</urap:ForeignCurrencyCode>")
	            .append("<urap:PaymentBankCode>").append(uraPaymentBankCode).append("</urap:PaymentBankCode>")
	            .append("<urap:PaymentMode>").append(uraPaymentMode).append("</urap:PaymentMode>")
	            .append("<urap:PaymentType>").append(uraPaymentType).append("</urap:PaymentType>")
	            .append("<urap:ReferenceNo>").append(uraReferenceNo).append("</urap:ReferenceNo>")
	            .append("<urap:SRCSystem>").append(uraSrcSystem).append("</urap:SRCSystem>")
	            .append("<urap:TaxHead>").append(requestDTO.getTaxHead()).append("</urap:TaxHead>")
	            .append("<urap:TaxPayerBankCode>").append(uraTaxPayerBankCode).append("</urap:TaxPayerBankCode>")
	            .append("<urap:TaxPayerName>").append(requestDTO.getTaxPayerName()).append("</urap:TaxPayerName>");

	    if (requestDTO.getTaxPayerNIN() != null &&
	        !requestDTO.getTaxPayerNIN().trim().isEmpty()) {

	        soapRequest.append("<urap:TaxPayerNIN>")
	                .append(requestDTO.getTaxPayerNIN())
	                .append("</urap:TaxPayerNIN>");
	    }

	    soapRequest.append("</tem:PRNRequest>")
	            .append("<tem:concatenatedUsernamePasswordSignature>")
	            .append(requestDTO.getConcatenatedUsernamePasswordSignature())
	            .append("</tem:concatenatedUsernamePasswordSignature>")
	            .append("<tem:concatenatedUsernamePassword>")
	            .append(requestDTO.getEncryptedConcatenatedUsernamePassword())
	            .append("</tem:concatenatedUsernamePassword>")
	            .append("<tem:userName>")
	            .append(requestDTO.getUserName())
	            .append("</tem:userName>")
	            .append("</tem:GetPRN_Foreign_Currency>")
	            .append("</soapenv:Body>")
	            .append("</soapenv:Envelope>");

	    return soapRequest.toString();
	}


	/**
	 * Build the CheckPRNStatus SOAP request.
	 */
	private String buildCheckPRNStatusRequest(URASoapCheckPRNStatusRequestDTO requestDTO) {
		return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
				+ "xmlns:tem=\"http://tempuri.org/\">" + "<soapenv:Header/>" + "<soapenv:Body>" + "<tem:CheckPRNStatus>"
				+ "<tem:strPRN>" + requestDTO.getStrPRN() + "</tem:strPRN>"
				+ "<tem:concatenatedUsernamePasswordSignature>" + requestDTO.getConcatenatedUsernamePasswordSignature()
				+ "</tem:concatenatedUsernamePasswordSignature>" + "<tem:encryptedConcatenatedUsernamePassword>"
				+ requestDTO.getEncryptedConcatenatedUsernamePassword() + "</tem:encryptedConcatenatedUsernamePassword>"
				+ "<tem:userName>" + requestDTO.getUserName() + "</tem:userName>" + "</tem:CheckPRNStatus>"
				+ "</soapenv:Body>" + "</soapenv:Envelope>";
	}

	private URASoapCheckPRNStatusResponseDTO parseSoapMessage(SOAPMessage soapMessage) throws Exception {
		String soapBodyContent = extractSoapBodyContent((soapMessage.getSOAPBody()));
		//log.info("SOAP Body Content: {}", soapBodyContent);
		XmlMapper xmlMapper = new XmlMapper();
		return xmlMapper.readValue(soapBodyContent, URASoapCheckPRNStatusResponseDTO.class);
	}
	
	private URASoapGetPRNResponseDTO parseSoapMessageGetPRN(SOAPMessage soapMessage) throws Exception {
		String soapBodyContent = extractSoapBodyContent((soapMessage.getSOAPBody()));
		//log.info("SOAP Body Content: {}", soapBodyContent);
		XmlMapper xmlMapper = new XmlMapper();
		return xmlMapper.readValue(soapBodyContent, URASoapGetPRNResponseDTO.class);
	}
	
	private URASoapGetPRNForeignCurrencyResponseDTO parseSoapMessageGetPRNForeign(SOAPMessage soapMessage) throws Exception {

	    String soapBodyContent = extractSoapBodyContent((soapMessage.getSOAPBody()));

	    XmlMapper xmlMapper = new XmlMapper();

	    return xmlMapper.readValue(
	            soapBodyContent,
	            URASoapGetPRNForeignCurrencyResponseDTO.class
	    );
	}

	private static String extractSoapBodyContent(SOAPBody soapBody) throws SOAPException, TransformerException {

		// Check if the SOAP body has child nodes
		if (soapBody.hasChildNodes()) {
			NodeList nodes = soapBody.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					// Convert the node to a string and return it
					return nodeToString(node);
				}
			}
		}
		return "";
	}

	private static String nodeToString(Node node) throws TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		StringWriter stringWriter = new StringWriter();
		transformer.transform(new DOMSource(node), new StreamResult(stringWriter));
		return stringWriter.toString();
	}

	private SOAPMessage convertStringToSOAPMessage(String soapMessageString) throws Exception {

		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage message = messageFactory.createMessage();

		SOAPPart soapPart = message.getSOAPPart();
		StreamSource source = new StreamSource(new StringReader(soapMessageString));
		soapPart.setContent(source);

		message.saveChanges();
		return message;
	}

	public URACheckPRNStatusResultDTO parseCheckPRNStatusResponse(String response) throws Exception {
		SOAPMessage soapMessage = convertStringToSOAPMessage(response);
		URASoapCheckPRNStatusResponseDTO responseDTO = parseSoapMessage(soapMessage);
		URACheckPRNStatusResultDTO resultDTO = responseDTO.getCheckPRNStatusResult();
		return resultDTO;
	}

	public URAGetPRNResultDTO parseGetPRNResponse(String response) throws Exception {
		SOAPMessage soapMessage = convertStringToSOAPMessage(response);
		URASoapGetPRNResponseDTO responseDTO = parseSoapMessageGetPRN(soapMessage);
		URAGetPRNResultDTO resultDTO = responseDTO.getGetPRNResult();
		return resultDTO;
	}
	
	public URAGetPRNForeignCurrencyResultDTO parseGetPRNForeignCurrencyResponse(String response) throws Exception {
	    SOAPMessage soapMessage = convertStringToSOAPMessage(response);
	    URASoapGetPRNForeignCurrencyResponseDTO responseDTO =
	            parseSoapMessageGetPRNForeign(soapMessage);
	    return responseDTO.getGetPRNResult();
	}

}