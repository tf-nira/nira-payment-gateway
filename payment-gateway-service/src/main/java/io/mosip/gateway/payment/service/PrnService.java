package io.mosip.gateway.payment.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.gateway.payment.constants.NiraProcess;
import io.mosip.gateway.payment.constants.PrnStatusCode;
import io.mosip.gateway.payment.constants.TaxHeadCode;
import io.mosip.gateway.payment.dto.CheckPRNStatusUraRequestDTO;
import io.mosip.gateway.payment.dto.CheckPRNStatusUraResponseDTO;
import io.mosip.gateway.payment.dto.CheckPRNStatusUraResultDTO;
import io.mosip.gateway.payment.dto.ConsumePrnRequestDTO;
import io.mosip.gateway.payment.dto.ConsumePrnResponseDTO;
import io.mosip.gateway.payment.dto.ExceptionJSONInfoDTO;
import io.mosip.gateway.payment.dto.IsPrnRegInLogsRequestDTO;
import io.mosip.gateway.payment.dto.IsPrnRegInLogsResponseDTO;
import io.mosip.gateway.payment.dto.MainMosipResponseDTO;
import io.mosip.gateway.payment.dto.PrnPaymentStatusDTO;
import io.mosip.gateway.payment.dto.PrnsConsumedListMetaDTO;
import io.mosip.gateway.payment.dto.PrnsConsumedListViewDTO;
import io.mosip.gateway.payment.entity.PrnConsumedEntity;
import io.mosip.gateway.payment.entity.PrnTransactionEntity;
import io.mosip.gateway.payment.repository.PrnTransactionRepository;
import io.mosip.gateway.payment.repository.PrnConsumedRepository;

import lombok.extern.slf4j.Slf4j;


/**
 * This service class handles are operation in regards to a PRN verification and consumption
 * 
 * 
 * @author Ibrahim Nkambo
 */
@Service
@Slf4j
public class PrnService {

	
	@Autowired
	private PrnConsumedRepository prnConsumedRepository;
	
	@Autowired
	private PrnTransactionRepository prnTransactionLogRepository;
	
	@Value("${mosip.all.version}")
	private double version;
    
    @Value("${mosip.utc-datetime-pattern}")
	private String mosipDateTimeFormat;
    
    private final String createdBySystem = "SYSTEM"; 

    ObjectMapper objectMapper;
    
    /* URA API */
    @Value("${ura.mda.payment.service.api.check-prn-status}")
    private String uraApiCheckPrnStatusUrl;
    
    @Autowired
	RestTemplate restTemplate;
    

    /**
     * This method gets status of a PRN with the help of URA APIs
     * 
     * @param prnStatusRequestDTO
     * @return prnPaymentStatusDTO
     * @throws Exception
     */
	public PrnPaymentStatusDTO getPrnStatus(CheckPRNStatusUraRequestDTO prnStatusRequestDTO) throws Exception{
		log.info("Payment Service - Inside getPrnStatus method - Finding status of prn");
    	
		PrnPaymentStatusDTO prnPaymentStatusDTO = null;
		CheckPRNStatusUraResponseDTO checkPRNStatusUraResponseDTO = null;
		
		try {
			
			if(!Objects.isNull(prnStatusRequestDTO)) {
				if(prnStatusRequestDTO.getPrn() != null && !prnStatusRequestDTO.getPrn().trim().isEmpty()) {
					
					PrnConsumedEntity checkingAgainstPrnConsumedEntity = prnConsumedRepository.findByPrn(prnStatusRequestDTO.getPrn()); 
					
					if(!Objects.isNull(checkingAgainstPrnConsumedEntity)) {
						if(checkingAgainstPrnConsumedEntity.isPrnValid()) {
							prnPaymentStatusDTO = prepareResponseForPrnStatus(checkingAgainstPrnConsumedEntity, null);
						}
						else {
							checkPRNStatusUraResponseDTO = checkPrnStatusURA(prnStatusRequestDTO).getBody();
							prnPaymentStatusDTO = prepareResponseForPrnStatus(checkingAgainstPrnConsumedEntity, checkPRNStatusUraResponseDTO);
						}
					}
					else {
						checkPRNStatusUraResponseDTO = checkPrnStatusURA(prnStatusRequestDTO).getBody();
						prnPaymentStatusDTO = prepareResponseForPrnStatus(null, checkPRNStatusUraResponseDTO);
					}
				}
				else {
					log.error("Bad Request: PRN is missing or empty");
				}
			}
			else {
				log.error("Bad Request: Request {} missing");
			}
			
		}
		catch (Exception e) {
			log.error("Internal Error occured while contacting URA API" + e.getMessage());
		}
    	
        return prnPaymentStatusDTO;
    }
	
	/**
	 * This method
	 * 
	 * @param prnConsumedEntity
	 * @param checkPRNStatusUraResponseDTO
	 * @return PrnPaymentStatusDTO
	 * @throws JSONException
	 * @throws IOException
	 */
	private PrnPaymentStatusDTO prepareResponseForPrnStatus(PrnConsumedEntity prnConsumedEntity, CheckPRNStatusUraResponseDTO checkPRNStatusUraResponseDTO) throws JSONException, IOException {
		
		PrnPaymentStatusDTO prnPaymentStatusDTO = new PrnPaymentStatusDTO();
		objectMapper = new ObjectMapper();
		
		/* If PRN exists in DB and is of valid status */
		if(!Objects.isNull(prnConsumedEntity) && Objects.isNull(checkPRNStatusUraResponseDTO)){
			prnPaymentStatusDTO.setCode(200);
			prnPaymentStatusDTO.setMessage("Operation Successful - PRN Paid, Proceed");
			
			CheckPRNStatusUraResultDTO convertedObject;
			try {
				convertedObject =  objectMapper.readValue(prnConsumedEntity.getPrnData(), new TypeReference<CheckPRNStatusUraResultDTO>() {});
				prnPaymentStatusDTO.setData(convertedObject);
			} catch (Exception e) {
				log.error("Failed to convert prn status result into json {} string");
				e.printStackTrace();
			}
		}
		
		/* If PRN exists in DB but not valid */
		if(!Objects.isNull(checkPRNStatusUraResponseDTO) && !Objects.isNull(prnConsumedEntity)) {
			prnPaymentStatusDTO.setCode(checkPRNStatusUraResponseDTO.getCode());
			prnPaymentStatusDTO.setMessage(checkPRNStatusUraResponseDTO.getMessage());
			
			if(!Objects.isNull(checkPRNStatusUraResponseDTO.getData()) && checkPRNStatusUraResponseDTO.getData()!="") {
				
				org.json.JSONObject jsonObject = new org.json.JSONObject(objectMapper.writeValueAsString(checkPRNStatusUraResponseDTO.getData()));
				CheckPRNStatusUraResultDTO convertedObject =  objectMapper.readValue(jsonObject.toString(), new TypeReference<CheckPRNStatusUraResultDTO>() {});
				
				PrnConsumedEntity updatingConsumedEntity = prnConsumedRepository.findByPrn(prnConsumedEntity.getPrn());
				
				if(!convertedObject.getStatusCode().toString().equalsIgnoreCase(PrnStatusCode.PRN_STATUS_RECEIVED_CREDITED.getStatusCode())) {
					updatingConsumedEntity.setPrnValid(false);
				}
				else {
					updatingConsumedEntity.setPrnValid(true);
				}
				jsonObject.put("ProcessFlow", getProcessFlowForResponse(convertedObject.getTaxHeadCode()));
				updatingConsumedEntity.setPrnData(jsonObject.toString());
				updatingConsumedEntity.setUpBy(createdBySystem);
				updatingConsumedEntity.setUpdDatetime(LocalDateTime.now());
				
				//update to db for prn
				prnConsumedRepository.save(updatingConsumedEntity);
				prnPaymentStatusDTO.setData(objectMapper.readValue(jsonObject.toString(), new TypeReference<CheckPRNStatusUraResultDTO>() {}));
			}	
		}
		
		/* If PRN doesn't exist in  DB - Contact URA API */
		if(!Objects.isNull(checkPRNStatusUraResponseDTO) && Objects.isNull(prnConsumedEntity)) {
						
			prnPaymentStatusDTO.setCode(checkPRNStatusUraResponseDTO.getCode());
			prnPaymentStatusDTO.setMessage(checkPRNStatusUraResponseDTO.getMessage());
			
			if(!Objects.isNull(checkPRNStatusUraResponseDTO.getData()) && checkPRNStatusUraResponseDTO.getData()!="") {

				org.json.JSONObject jsonObject = new org.json.JSONObject(objectMapper.writeValueAsString(checkPRNStatusUraResponseDTO.getData()));
				CheckPRNStatusUraResultDTO convertedObject =  objectMapper.readValue(jsonObject.toString(), new TypeReference<CheckPRNStatusUraResultDTO>() {});

				PrnConsumedEntity newConsumedEntity = new PrnConsumedEntity();
				newConsumedEntity.setPrn(jsonObject.get("PRN").toString());
				
				if(!convertedObject.getStatusCode().toString().equalsIgnoreCase(PrnStatusCode.PRN_STATUS_RECEIVED_CREDITED.getStatusCode())) {
					newConsumedEntity.setPrnValid(false);
				}
				else {
					newConsumedEntity.setPrnValid(true);
				}
				
				jsonObject.put("ProcessFlow", getProcessFlowForResponse(convertedObject.getTaxHeadCode()));
				newConsumedEntity.setPrnData(jsonObject.toString());
				newConsumedEntity.setCrBy(createdBySystem);
				newConsumedEntity.setCrDatetime(LocalDateTime.now());
				
				//save to db for prn
				prnConsumedRepository.save(newConsumedEntity);
				prnPaymentStatusDTO.setData(objectMapper.readValue(jsonObject.toString(), new TypeReference<CheckPRNStatusUraResultDTO>() {}));
			}
			else {
				prnPaymentStatusDTO.setData(null);
			}
			
		}
		return prnPaymentStatusDTO;
		
	}
	
	private String getProcessFlowForResponse(String taxHeadCode) {

		if(taxHeadCode.equalsIgnoreCase(TaxHeadCode.TAX_HEAD_CHANGE.getTaxHeadCode()) || 
				taxHeadCode.equalsIgnoreCase(TaxHeadCode.TAX_HEAD_CORRECTION_ERRORS.getTaxHeadCode())) {
			return NiraProcess.UPDATE.getProcess();
		}
		
		else if(taxHeadCode.equalsIgnoreCase(TaxHeadCode.TAX_HEAD_REPLACE.getTaxHeadCode()) || 
				taxHeadCode.equalsIgnoreCase(TaxHeadCode.TAX_HEAD_REPLACE_DEFACED.getTaxHeadCode())) {
			return NiraProcess.REPLACE.getProcess();
		}
		return null;
	}
	
	/**
	 * This method returns a list of all consumed PRNs
	 * 
	 * @return list of all prns
	 */
	public MainMosipResponseDTO<PrnsConsumedListMetaDTO> findAllConsumedPrns(){
		log.info("Payment Service - Inside findAllConsumedPrns method - Finding all  prns consumed");
		MainMosipResponseDTO<PrnsConsumedListMetaDTO> response = new MainMosipResponseDTO<PrnsConsumedListMetaDTO>();
		PrnsConsumedListMetaDTO prnsListMetaDTO = new PrnsConsumedListMetaDTO();
		
		objectMapper = new ObjectMapper();
		
		response.setVersion(String.valueOf(version));
		response.setResponsetime(DateTimeFormatter.ofPattern(mosipDateTimeFormat).format(LocalDateTime.now()));
		
		List<ExceptionJSONInfoDTO> explist = new ArrayList<ExceptionJSONInfoDTO>();
		ExceptionJSONInfoDTO exception = new ExceptionJSONInfoDTO();
		
		try {
			
			List<PrnConsumedEntity> listPrns = prnConsumedRepository.findAll();
	
			if(!Objects.isNull(listPrns)) {
			
				List<PrnsConsumedListViewDTO> viewList = new ArrayList<>();

		    	for (PrnConsumedEntity prnTransactionEntity : listPrns) {		
					PrnsConsumedListViewDTO viewDto = new PrnsConsumedListViewDTO();
					viewDto.setPrn(prnTransactionEntity.getPrn());
					
					CheckPRNStatusUraResultDTO	convertedObject =  objectMapper.readValue(prnTransactionEntity.getPrnData(), new TypeReference<CheckPRNStatusUraResultDTO>() {});					
					viewDto.setPrnData(convertedObject);
					viewDto.setPrnValid(prnTransactionEntity.isPrnValid());
					
					viewDto.setDateCreated(prnTransactionEntity.getCrDatetime());
					viewDto.setDateUpdated(prnTransactionEntity.getUpdDatetime());
					viewList.add(viewDto);
		    	}
		    	
		    	prnsListMetaDTO.setPrns(viewList);
				prnsListMetaDTO.setTotalRecords(Integer.toString(listPrns.size()));	
				response.setResponse(prnsListMetaDTO);
			}
			else {
				log.error("Payment Service - Inside findAllConsumedPrns method - Response: No PRNs found");
				exception.setMessage("No PRNs found");
				explist.add(exception);
				response.setErrors(explist);

			}
		} catch (Exception ex) {
			exception.setMessage("Internal Error while finding transacted prns: " + ex.getStackTrace());
			explist.add(exception);
			response.setErrors(explist);
		}
		return response;
	}

	/**
	 * This method consumes (saves) a PRN to database with the registrationId
	 * 
	 * @param requestDTO
	 * @return ConsumePrnResponseDTO
	 */
	public MainMosipResponseDTO<ConsumePrnResponseDTO> consumePrnAsUsed(ConsumePrnRequestDTO requestDTO){
		log.info("Payment Service - Inside consumePrnAsUsed method - Consume a PRN");
		MainMosipResponseDTO<ConsumePrnResponseDTO> response = new MainMosipResponseDTO<ConsumePrnResponseDTO>();
		ConsumePrnResponseDTO consumePrnResponseDTO = new ConsumePrnResponseDTO();
		
		response.setVersion(String.valueOf(version));
		response.setResponsetime(DateTimeFormatter.ofPattern(mosipDateTimeFormat).format(LocalDateTime.now()));
		
		List<ExceptionJSONInfoDTO> explist = new ArrayList<ExceptionJSONInfoDTO>();
		ExceptionJSONInfoDTO exception = new ExceptionJSONInfoDTO();
		
		try {
			
			if(!Objects.isNull(requestDTO)) {
			
				consumePrnResponseDTO.setPrnNum(requestDTO.getPrn());
				
				PrnTransactionEntity existingPrnTransactionEntity = prnTransactionLogRepository.findByPrn(requestDTO.getPrn());
				
				if(!Objects.isNull(existingPrnTransactionEntity)) {
					log.info("Payment Service - Inside consumePrnAsUsed method - Consuming failed -> PRN was already consumed");
					consumePrnResponseDTO.setRegIdTaggedToPrn(existingPrnTransactionEntity.getRegId());
					consumePrnResponseDTO.setConsumedSucess(false);
					response.setResponse(consumePrnResponseDTO);
					exception.setMessage("PRN was already consumed");
					explist.add(exception);
					response.setErrors(explist);
				}
				else {
					/* save to db for prn_transaction_logs */
					PrnTransactionEntity  prnTransactionLogEntity = new PrnTransactionEntity();
					prnTransactionLogEntity.setPrn(requestDTO.getPrn());
					prnTransactionLogEntity.setRegId(requestDTO.getRegId());
					prnTransactionLogEntity.setCrBy(createdBySystem);
					prnTransactionLogEntity.setCrDatetime(LocalDateTime.now());
					prnTransactionLogRepository.save(prnTransactionLogEntity);
					
					log.info("Payment Service - Inside consumePrnAsUsed method - Consuming success -> Added record to consumption logs");
					consumePrnResponseDTO.setConsumedSucess(true);
					consumePrnResponseDTO.setRegIdTaggedToPrn(requestDTO.getRegId());
					response.setResponse(consumePrnResponseDTO);
				}
			}
			else {
				log.error("Bad request. Request {} missing");
				exception.setMessage("Bad request. Request {} missing");
				explist.add(exception);
				response.setErrors(explist);
			}
	
		} catch (Exception e) {
			log.error("PRN consumption failed." + e.getMessage());
			exception.setMessage("Internal Error while consuming a PRN: " + e.getMessage());
			explist.add(exception);
			response.setErrors(explist);
		}
		
		return response;
	}
	
	/**
	 * This method checks the transcation logs table if the PRN and registrationId are present
	 * 
	 * @param isPrnRegInLogsRequestDTO
	 * @return ispresent status
	 */
	public MainMosipResponseDTO<IsPrnRegInLogsResponseDTO> checkIfPrnAndRegIdPresentInLogs(IsPrnRegInLogsRequestDTO isPrnRegInLogsRequestDTO) {
		
		log.info("Payment Service - Inside checkIfPrnAndRegIdPresentInLogs method - Check if PRN and Reg Id are in transaction logs");
		MainMosipResponseDTO<IsPrnRegInLogsResponseDTO> response = new MainMosipResponseDTO<IsPrnRegInLogsResponseDTO>();
		IsPrnRegInLogsResponseDTO isPrnRegInLogsResponseDTO = new IsPrnRegInLogsResponseDTO();
		
		response.setVersion(String.valueOf(version));
		response.setResponsetime(DateTimeFormatter.ofPattern(mosipDateTimeFormat).format(LocalDateTime.now()));
		
		List<ExceptionJSONInfoDTO> explist = new ArrayList<ExceptionJSONInfoDTO>();
		ExceptionJSONInfoDTO exception = new ExceptionJSONInfoDTO();
		
		
		
		try {
			
			if(!Objects.isNull(isPrnRegInLogsRequestDTO) && isPrnRegInLogsRequestDTO.getPrn()!=null &&
					isPrnRegInLogsRequestDTO.getRegId()!=null && !isPrnRegInLogsRequestDTO.getPrn().trim().isEmpty() &&
					!isPrnRegInLogsRequestDTO.getRegId().trim().isEmpty()) {
				PrnTransactionEntity prnTransactionLogEntity = prnTransactionLogRepository.findByPrn(isPrnRegInLogsRequestDTO.getPrn());
			
				if(!Objects.isNull(prnTransactionLogEntity)){
					// To allow for reprocessing
					if(prnTransactionLogEntity.getRegId().equals(isPrnRegInLogsRequestDTO.getRegId())) {
						isPrnRegInLogsResponseDTO.setPrn(isPrnRegInLogsRequestDTO.getPrn());
						isPrnRegInLogsResponseDTO.setRegId(isPrnRegInLogsRequestDTO.getRegId());
						isPrnRegInLogsResponseDTO.setPresentInLogs(true);
					}
					else {
						isPrnRegInLogsResponseDTO.setPrn(isPrnRegInLogsRequestDTO.getPrn());
						isPrnRegInLogsResponseDTO.setPresentInLogs(true);
						exception.setMessage("PRN Consumed by different Reg Id: " + prnTransactionLogEntity.getRegId());
						explist.add(exception);
						response.setErrors(explist);
					}
				}
				else {
					isPrnRegInLogsResponseDTO.setPresentInLogs(false);
				}
				response.setResponse(isPrnRegInLogsResponseDTO);
			}
			else {
				log.error("Bad request. Request {} missing");
				exception.setMessage("Bad request. Request {} missing");
				explist.add(exception);
				response.setErrors(explist);
			}
			
			
		}
		catch (Exception e) {
			log.error("Checking for prn and regId in logs failed." + e.getStackTrace());
			exception.setMessage("Internal Error while checking if prn and reg id exists in logs: " + e.getMessage());
			explist.add(exception);
			response.setErrors(explist);
		}
		return response;
	}
	
	protected <T> T convertToObject(byte[] identity, Class<T> clazz) throws Exception {
		try {
			
			objectMapper = new ObjectMapper();
			//String s = Base64.getEncoder().encodeToString(identity);
			String s = new String(identity);
			return objectMapper.readValue(s, clazz);
		} catch (IOException e) {
			log.error("convertToObject", e.getMessage());
			throw new Exception(e);
		}
	}

	protected byte[] convertToBytes(Object identity) throws Exception {
		try {
			objectMapper = new ObjectMapper();
			return objectMapper.writeValueAsBytes(identity);
		} catch (JsonProcessingException e) {
			log.error("convertToBytes", e.getMessage());
			throw new Exception(e);
		}
	}
	
	/**
	 * This method calls an external URA API to get the status of a PRN
	 * 
	 * @param prnStatusRequestDTO
	 * @return PRN response object
	 */
	private ResponseEntity<CheckPRNStatusUraResponseDTO> checkPrnStatusURA(CheckPRNStatusUraRequestDTO prnStatusRequestDTO) {
		
		log.info("Contacting URA for PRN status");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		ResponseEntity<CheckPRNStatusUraResponseDTO> response = null;

			response =
				    restTemplate.exchange(uraApiCheckPrnStatusUrl,
				                          HttpMethod.POST,
				                          new HttpEntity<>(prnStatusRequestDTO, headers),
				                          CheckPRNStatusUraResponseDTO.class);

		return response;
	}
	

}
