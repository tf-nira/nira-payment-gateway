package io.mosip.gateway.payment.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;


import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.gateway.payment.dto.test.TestSoapRequestDTO;
import io.mosip.gateway.payment.dto.test.TestSoapResponseDTO;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPConnection;
import jakarta.xml.soap.SOAPConnectionFactory;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.soap.SOAPPart;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SoapServiceUtil {

	@Autowired
	Jaxb2Marshaller jaxb2Marshaller;

	@SuppressWarnings("unchecked")
	public <T> T unmarshall(SOAPMessage soapMessage, Class<T> type) 
		throws XmlMappingException, IOException, SOAPException {

		T jaxbElement = (T) jaxb2Marshaller.unmarshal(
			new DOMSource(soapMessage.getSOAPBody().extractContentAsDocument()));
		return jaxbElement;
	}
	
	private static void createSoapEnvelope(SOAPMessage soapMessage, TestSoapRequestDTO testSoapRequestDTO)
			throws SOAPException {
		SOAPPart soapPart = soapMessage.getSOAPPart();

		String myNamespace = "web";
		String myNamespaceURI = "http://www.dataaccess.com/webservicesserver/";

		// SOAP Envelope
		SOAPEnvelope envelope = soapPart.getEnvelope();
		envelope.addNamespaceDeclaration(myNamespace, myNamespaceURI);

		/*
		 * Constructed SOAP Request Message: 
		 * <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:web="http://www.dataaccess.com/webservicesserver/">
		 * 		<soapenv:Header/>
		 * 		<soapenv:Body>
		 * 			<web:NumberToDollars>
		 * 				<web:dNum>10</web:dNum>
		 * 			</web:NumberToDollars>
		 * 		</soapenv:Body>
		 * </soapenv:Envelope>
		 */
		
		/*
		 * Sample Constructed URA SOAP Request Message for Check PRN Status:
		 * 
		 * <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tem="http://tempuri.org/">
			   <soapenv:Header/>
			   <soapenv:Body>
			      <tem:CheckPRNStatus>
			         <tem:strPRN>2220000000356</tem:strPRN>
			         <tem:concatenatedUsernamePasswordSignature>LKOSDUhOB18AW7qUwkNNRXNmeGg==</tem:concatenatedUsernamePasswordSignature>
			         <tem:encryptedConcatenatedUsernamePassword>bxXlIfdfQ3DUhOB18AW7qUwkNNRXNmeGg==</tem:encryptedConcatenatedUsernamePassword>
			         <tem:userName>TEST</tem:userName>
			      </tem:CheckPRNStatus>
			   </soapenv:Body>
			</soapenv:Envelope>
		 */

		// SOAP Body
		SOAPBody soapBody = envelope.getBody();
		
		// For Test
		SOAPElement soapBodyElem = soapBody.addChildElement("NumberToDollars", myNamespace);
		SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("dNum", myNamespace);
		soapBodyElem1.addTextNode(testSoapRequestDTO.getNumber());
		
		// For MDAPaymentService
		/*SOAPElement soapBodyElement = soapBody.addChildElement("CheckPRNStatus", myNamespace);
		SOAPElement soapBodyPrnElement = soapBodyElement.addChildElement("strPRN", myNamespace);
		SOAPElement soapBodySignElement = soapBodyElement.addChildElement("concatenatedUsernamePasswordSignature", myNamespace);
		SOAPElement soapBodyEncryptedElement = soapBodyElement.addChildElement("encryptedConcatenatedUsernamePassword", myNamespace);
		SOAPElement soapBodyuserNameElement = soapBodyElement.addChildElement("userName", myNamespace);
		soapBodyPrnElement.addTextNode(checkPRNStatusUraRequestDTO.getPrn());
		soapBodySignElement.addTextNode(checkPRNStatusUraRequestDTO.getSignedCredentials());
		soapBodyEncryptedElement.addTextNode(checkPRNStatusUraRequestDTO.getEncryptedCredentials());
		soapBodyuserNameElement.addTextNode(checkPRNStatusUraRequestDTO.getUserName());*/
	}

	public String callSoapWebService(String soapEndpointUrl, String soapAction, TestSoapRequestDTO testSoapRequestDTO) {
		
		String stringExtracted = null;
		
		try {
			// Create SOAP Connection
			SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
			SOAPConnection soapConnection = soapConnectionFactory.createConnection();

			// Send SOAP Message to SOAP Server
			SOAPMessage soapResponse = soapConnection.call(createSOAPRequest(soapAction, testSoapRequestDTO),
					soapEndpointUrl);
			String soapResponseString = getStringFromSOAPMessage(soapResponse);
			// Print the SOAP Response
			log.info("Soap Message Response: " + soapResponseString);
			
			stringExtracted = convertSoapToJson(soapResponse);
			
			log.info("Response in JSON: " + stringExtracted);
			
			soapConnection.close();
			
		} catch (Exception e) {

			log.error("Error occurred while sending SOAP Request to Server!");
			e.printStackTrace();
		}
		
		return stringExtracted;
		
	}

	private static SOAPMessage createSOAPRequest(String soapAction, TestSoapRequestDTO testSoapRequestDTO)
			throws Exception {
		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage soapMessage = messageFactory.createMessage();

		createSoapEnvelope(soapMessage, testSoapRequestDTO);

		MimeHeaders headers = soapMessage.getMimeHeaders();
		headers.addHeader("SOAPAction", soapAction);

		soapMessage.saveChanges();

		/* Print the request message, just for debugging purposes */
		log.error("Soap Message Request :" + getStringFromSOAPMessage(soapMessage));

		return soapMessage;
	}
	
	private static String getStringFromSOAPMessage(SOAPMessage soapMessage) {
		
		final StringWriter sw = new StringWriter();
		try {
			TransformerFactory.newInstance().newTransformer().transform(
			        new DOMSource(soapMessage.getSOAPPart()),
			        new StreamResult(sw));
		} catch (TransformerException | TransformerFactoryConfigurationError e) {
			e.printStackTrace();
			return "{\"error\": \"Failed to convert SOAP to String\"}";
			
		}
		
		return sw.toString();
	}
	
	public static <T> T getObjectFromSOAPMessageUsingXMLStreamReader(SOAPMessage soapMessage, Class<T> type) {
		
		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		XMLStreamReader xmlStreamReader = null;
		JAXBElement<T> jaxbElement = null;
		try {
			xmlStreamReader = xmlInputFactory.createXMLStreamReader(new StringReader(getStringFromSOAPMessage(soapMessage)));
			xmlStreamReader.nextTag();
			while (!xmlStreamReader.getLocalName().equals(type.getSimpleName())) {
				xmlStreamReader.nextTag();
			}
			Unmarshaller unmarshaller = JAXBContext.newInstance(type).createUnmarshaller();
			jaxbElement = unmarshaller.unmarshal(xmlStreamReader, type);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				xmlStreamReader.close();
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
		}
		return jaxbElement.getValue();
	}
	
    private String convertSoapToJson(SOAPMessage soapMessage) { 
    	ObjectMapper mapper = new ObjectMapper();
    	
        try {
        	//CheckPRNStatusUraResultDTO responsOneMore = unmarshall(soapMessage, CheckPRNStatusUraResultDTO.class);
        	TestSoapResponseDTO responseOneMore = unmarshall(soapMessage, TestSoapResponseDTO.class);      	
        	return mapper.writeValueAsString(responseOneMore);
            
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to convert SOAP to JSON\"}";
        }
    }


}
