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
import io.mosip.gateway.payment.dto.ura.URAGetPRNResultDTO;
import io.mosip.gateway.payment.dto.ura.URASoapCheckPRNStatusRequestDTO;
import io.mosip.gateway.payment.dto.ura.URASoapCheckPRNStatusResponseDTO;
import io.mosip.gateway.payment.dto.ura.URASoapGeneratePRNRequestDTO;
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

	@Autowired
	Jaxb2Marshaller jaxb2Marshaller;

	/**
	 * Method to make the GetPRN SOAP request.
	 */
	public String getPRN(URASoapGeneratePRNRequestDTO requestDTO) {
		String soapRequest = buildGetPRNRequest(requestDTO);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_XML);
		headers.add("SOAPAction", uraGetPRNSOAPAction);

		HttpEntity<String> entity = new HttpEntity<>(soapRequest, headers);

		ResponseEntity<String> response = restTemplate.exchange(uraWsdl, HttpMethod.POST, entity, String.class);

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
	private String buildGetPRNRequest(URASoapGeneratePRNRequestDTO requestDTO) {
		return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
				+ "xmlns:tem=\"http://tempuri.org/\" xmlns:urap=\"http://schemas.datacontract.org/2004/07/URAPaymentGateway.DataContracts\">"
				+ "<soapenv:Header/>" + "<soapenv:Body>" + "<tem:GetPRN>" + "<tem:PRNRequest>" + "<urap:Amount>"
				+ requestDTO.getAmount() + "</urap:Amount>" + "<urap:ExpiryDays>21</urap:ExpiryDays>"
				+ "<urap:PaymentBankCode>STN</urap:PaymentBankCode>" + "<urap:PaymentMode>CASH</urap:PaymentMode>"
				+ "<urap:PaymentType>DT</urap:PaymentType>" + "<urap:ReferenceNo>" + requestDTO.getReferenceNo()
				+ "</urap:ReferenceNo>" + "<urap:SRCSystem>NIRA</urap:SRCSystem>" + "<urap:TaxHead>"
				+ requestDTO.getTaxHead() + "</urap:TaxHead>" + "<urap:TaxPayerBankCode>STN</urap:TaxPayerBankCode>"
				+ "<urap:TaxPayerName>" + requestDTO.getTaxPayerName() + "</urap:TaxPayerName>" + "<urap:TaxPayerNIN>"
				+ requestDTO.getTaxPayerNIN() + "</urap:TaxPayerNIN>" + "</tem:PRNRequest>"
				+ "<tem:concatenatedUsernamePasswordSignature>" + requestDTO.getConcatenatedUsernamePasswordSignature()
				+ "</tem:concatenatedUsernamePasswordSignature>" + "<tem:encryptedConcatenatedUsernamePassword>"
				+ requestDTO.getEncryptedConcatenatedUsernamePassword() + "</tem:encryptedConcatenatedUsernamePassword>"
				+ "<tem:userName>" + requestDTO.getUserName() + "</tem:userName>" + "</tem:GetPRN>" + "</soapenv:Body>"
				+ "</soapenv:Envelope>";
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

}