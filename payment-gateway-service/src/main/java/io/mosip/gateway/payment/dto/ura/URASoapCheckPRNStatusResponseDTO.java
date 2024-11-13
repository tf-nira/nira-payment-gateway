package io.mosip.gateway.payment.dto.ura;


import java.io.Serializable;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "CheckPRNStatusResponse", namespace = "http://tempuri.org/")
public class URASoapCheckPRNStatusResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JacksonXmlProperty(localName = "CheckPRNStatusResult", namespace = "http://schemas.datacontract.org/2004/07/Entities.Models")
    private URACheckPRNStatusResultDTO checkPRNStatusResult;

}

