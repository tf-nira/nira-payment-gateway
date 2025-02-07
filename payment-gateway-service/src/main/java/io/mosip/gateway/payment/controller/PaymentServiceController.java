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

import io.mosip.gateway.payment.dto.request.CheckPRNStatusRequestDTO;
import io.mosip.gateway.payment.dto.request.CheckPRNStatusResultDTO;
import io.mosip.gateway.payment.dto.request.ConsumePRNRequestDTO;
import io.mosip.gateway.payment.dto.request.GeneratePRNRequestDTO;
import io.mosip.gateway.payment.dto.request.IsPRNRegInLogsRequestDTO;
import io.mosip.gateway.payment.dto.response.ConsumePRNResponseDTO;
import io.mosip.gateway.payment.dto.response.GeneratePRNResponseDTO;
import io.mosip.gateway.payment.dto.response.IsPRNRegInLogsResponseDTO;
import io.mosip.gateway.payment.dto.response.MainMosipResponseDTO;
import io.mosip.gateway.payment.dto.response.PRNGeneratedDTO;
import io.mosip.gateway.payment.dto.response.PRNStatusDTO;
import io.mosip.gateway.payment.dto.response.PrnsConsumedListMetaDTO;
import io.mosip.gateway.payment.service.PrnService;
import io.swagger.v3.oas.annotations.Operation;

@RestController
public class PaymentServiceController {
	
	private final PrnService prnService;
	
	public PaymentServiceController(PrnService prnService) {
		this.prnService = prnService;
	}
	
	@PostMapping("/checkPrnStatus")
	@Operation(summary = "checkPrnStatus", description = "Fetch the status of a given prn", tags = "payment-service-controller")
	public ResponseEntity<MainMosipResponseDTO<CheckPRNStatusResultDTO>> checkPrnStatus(
			@Valid @RequestBody(required = false) CheckPRNStatusRequestDTO prnStatusRequestDTO) throws Exception{
		
		return ResponseEntity.status(HttpStatus.OK)
				.body(prnService.getPrnStatus(prnStatusRequestDTO));
	}
	
	@PostMapping("/generatePrn")
	@Operation(summary = "generatePRN", description = "Generate a new PRN", tags = "payment-service-controller")
	public ResponseEntity<MainMosipResponseDTO<PRNGeneratedDTO>> generatePRN(	
			@Valid @RequestBody(required = false) GeneratePRNRequestDTO generatePRNRequestDTO) throws Exception {
		return ResponseEntity.status(HttpStatus.OK).body(prnService.generatePrn(generatePRNRequestDTO));
	}
	
	
	/*
	@GetMapping("/getAllConsumedPrns")
	@Operation(summary = "getAllConsumedPrns", description = "Fetch all consumed prns", tags = "payment-service-controller")
	public ResponseEntity<MainMosipResponseDTO<PrnsConsumedListMetaDTO>> getAllConsumedPrns(){
		
		return ResponseEntity.status(HttpStatus.OK)
					.body(prnService.findAllConsumedPrns());
	}*/
	
	@PostMapping("/consumePrn")
	@Operation(summary = "consumePrn", description = "Consume PRN as used", tags = "payment-service-controller")
	public ResponseEntity<MainMosipResponseDTO<ConsumePRNResponseDTO>> consumePrn(
			@Valid @RequestBody(required = false) ConsumePRNRequestDTO consumePrnRequestDTO) throws Exception{
		
		return ResponseEntity.status(HttpStatus.OK)
				.body(prnService.consumePrnAsUsed(consumePrnRequestDTO));
		
	}
	
	@PostMapping("/checkTranscLogs")
	@Operation(summary = "checkTranscLogs", description = "Check Transaction Logs for Reg Id and PRN", tags = "payment-service-controller")
	public ResponseEntity<MainMosipResponseDTO<IsPRNRegInLogsResponseDTO>> checkTranscLogs(
			@Valid @RequestBody(required = false) IsPRNRegInLogsRequestDTO isPrnRegInLogsRequestDTO) throws Exception{
		
		return ResponseEntity.status(HttpStatus.OK)
				.body(prnService.checkIfPrnAndRegIdPresentInLogs(isPrnRegInLogsRequestDTO));
		
	}
}
