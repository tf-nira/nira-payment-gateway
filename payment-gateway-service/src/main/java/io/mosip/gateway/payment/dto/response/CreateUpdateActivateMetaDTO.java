package io.mosip.gateway.payment.dto.response;

import lombok.Data;

@Data
public class CreateUpdateActivateMetaDTO<T> {
	
	 private String id;
	 private String status;
	 private T data;

}
