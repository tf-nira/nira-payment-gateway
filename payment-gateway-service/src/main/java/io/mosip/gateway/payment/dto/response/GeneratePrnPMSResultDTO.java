package io.mosip.gateway.payment.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneratePrnPMSResultDTO {

    private String prn;
    private String currency;
    private String expiryDate;
    private Double amount;
    private String errorCode;
    private String errorDesc;
    private Integer numberOfRecords;
}
