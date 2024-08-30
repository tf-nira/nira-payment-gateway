package io.mosip.gateway.payment.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.json.JSONObject;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrnsConsumedListViewDTO {
	
	private String prn;
	private CheckPRNStatusUraResultDTO prnData;
	private boolean isPrnValid;
	private LocalDateTime dateCreated;
	private LocalDateTime dateUpdated;
}
