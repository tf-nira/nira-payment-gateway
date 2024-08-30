package io.mosip.gateway.payment.controller;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.gateway.payment.dto.CheckPRNStatusUraRequestDTO;
import io.mosip.gateway.payment.dto.ConsumePrnRequestDTO;
import io.mosip.gateway.payment.dto.ConsumePrnResponseDTO;
import io.mosip.gateway.payment.dto.IsPrnRegInLogsRequestDTO;
import io.mosip.gateway.payment.dto.IsPrnRegInLogsResponseDTO;
import io.mosip.gateway.payment.dto.MainMosipResponseDTO;
import io.mosip.gateway.payment.dto.PrnPaymentStatusDTO;
import io.mosip.gateway.payment.dto.PrnsConsumedListMetaDTO;
import io.mosip.gateway.payment.service.PrnService;
import io.swagger.v3.oas.annotations.Operation;

@RestController
public class PaymentServiceController {
	
	private final PrnService prnService;
	
	public PaymentServiceController(PrnService prnService) {
		this.prnService = prnService;
	}
	
	@PreAuthorize("hasAnyAuthority('ROLE_USER')")
	@PostMapping("/getPrnStatus")
	@Operation(summary = "getPrnStatus", description = "Fetch the status of a given preregistration Id", tags = "payment-service-controller")
	public ResponseEntity<PrnPaymentStatusDTO> getPrnStatus(
			@Valid @RequestBody(required = false) CheckPRNStatusUraRequestDTO prnStatusRequestDTO) throws Exception{
		
		return ResponseEntity.status(HttpStatus.OK)
				.body(prnService.getPrnStatus(prnStatusRequestDTO));
	}
	
	@PreAuthorize("hasAnyAuthority('ROLE_USER')")
	@GetMapping("/getAllConsumedPrns")
	@Operation(summary = "getAllConsumedPrns", description = "Fetch all consumed prns", tags = "payment-service-controller")
	public ResponseEntity<MainMosipResponseDTO<PrnsConsumedListMetaDTO>> getAllConsumedPrns(){
		
		return ResponseEntity.status(HttpStatus.OK)
					.body(prnService.findAllConsumedPrns());
	}
	
	@PreAuthorize("hasAnyAuthority('ROLE_USER')")
	@PostMapping("/consumePrn")
	@Operation(summary = "consumePrn", description = "Consume PRN as used", tags = "payment-service-controller")
	public ResponseEntity<MainMosipResponseDTO<ConsumePrnResponseDTO>> consumePrn(
			@Valid @RequestBody(required = false) ConsumePrnRequestDTO consumePrnRequestDTO) throws Exception{
		
		return ResponseEntity.status(HttpStatus.OK)
				.body(prnService.consumePrnAsUsed(consumePrnRequestDTO));
		
	}
	
	@PreAuthorize("hasAnyAuthority('ROLE_USER')")
	@PostMapping("/checkTranscLogs")
	@Operation(summary = "checkTranscLogs", description = "Check Transaction Logs for Reg Id and PRN", tags = "payment-service-controller")
	public ResponseEntity<MainMosipResponseDTO<IsPrnRegInLogsResponseDTO>> checkTranscLogs(
			@Valid @RequestBody(required = false) IsPrnRegInLogsRequestDTO isPrnRegInLogsRequestDTO) throws Exception{
		
		return ResponseEntity.status(HttpStatus.OK)
				.body(prnService.checkIfPrnAndRegIdPresentInLogs(isPrnRegInLogsRequestDTO));
		
	}
}
