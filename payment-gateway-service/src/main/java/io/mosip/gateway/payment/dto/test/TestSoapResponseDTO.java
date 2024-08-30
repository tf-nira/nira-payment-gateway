package io.mosip.gateway.payment.dto.test;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;


@Data
@XmlRootElement(name = "NumberToDollarsResponse", namespace = "http://www.dataaccess.com/webservicesserver/")
@XmlAccessorType(XmlAccessType.FIELD)
public class TestSoapResponseDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@XmlElement(name="NumberToDollarsResult", namespace = "http://www.dataaccess.com/webservicesserver/")
	private String numberToDollarsResult;
}
