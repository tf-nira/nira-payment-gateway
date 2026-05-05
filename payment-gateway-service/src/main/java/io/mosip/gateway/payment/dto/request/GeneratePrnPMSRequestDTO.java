package io.mosip.gateway.payment.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneratePrnPMSRequestDTO {

    private String partnerName;
    private String partnerType;
    private String partnerGroup;
    private Integer numberOfRecords;
}