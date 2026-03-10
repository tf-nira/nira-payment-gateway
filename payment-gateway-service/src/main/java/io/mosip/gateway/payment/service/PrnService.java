package io.mosip.gateway.payment.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.gateway.payment.client.UraPaymentClient;
import io.mosip.gateway.payment.constants.AppConstants;
import io.mosip.gateway.payment.constants.AppErrorMessages;
import io.mosip.gateway.payment.constants.AppLogMessages;
import io.mosip.gateway.payment.constants.AppSuccessMessages;
import io.mosip.gateway.payment.constants.PrnStatusCode;
import io.mosip.gateway.payment.dto.request.CheckPRNStatusPMSResultDTO;
import io.mosip.gateway.payment.dto.request.CheckPRNStatusRequestDTO;
import io.mosip.gateway.payment.dto.request.CheckPRNStatusResultDTO;
import io.mosip.gateway.payment.dto.request.ConsumePRNRequestDTO;
import io.mosip.gateway.payment.dto.request.GeneratePRNRequestDTO;
import io.mosip.gateway.payment.dto.request.GeneratePrnPMSRequestDTO;
import io.mosip.gateway.payment.dto.request.IsPRNRegInLogsRequestDTO;
import io.mosip.gateway.payment.dto.response.CheckPRNStatusResponseDTO;
import io.mosip.gateway.payment.dto.response.ConsumePRNResponseDTO;
import io.mosip.gateway.payment.dto.response.GeneratePRNResponseDTO;
import io.mosip.gateway.payment.dto.response.GeneratePRNResultDTO;
import io.mosip.gateway.payment.dto.response.GeneratePrnPMSResultDTO;
import io.mosip.gateway.payment.dto.response.IsPRNRegInLogsResponseDTO;
import io.mosip.gateway.payment.dto.response.MainMosipResponseDTO;
import io.mosip.gateway.payment.dto.response.PRNGeneratedDTO;
import io.mosip.gateway.payment.dto.response.PRNStatusDTO;
import io.mosip.gateway.payment.dto.response.PrnsConsumedListMetaDTO;
import io.mosip.gateway.payment.dto.response.PrnsConsumedListViewDTO;
import io.mosip.gateway.payment.dto.ura.URACheckPRNStatusResultDTO;
import io.mosip.gateway.payment.dto.ura.URAGetPRNResultDTO;
import io.mosip.gateway.payment.entity.PayableServiceTypeEntity;
import io.mosip.gateway.payment.entity.PmsServiceEntity;
import io.mosip.gateway.payment.entity.PrnConsumedEntity;
import io.mosip.gateway.payment.entity.PrnTaxHeadEntity;
import io.mosip.gateway.payment.entity.PrnTransactionEntity;
import io.mosip.gateway.payment.repository.PrnTransactionRepository;
import io.mosip.gateway.payment.util.ResponseUtil;
import io.mosip.gateway.payment.repository.PayableServiceTypeRepository;
import io.mosip.gateway.payment.repository.PmsServiceRepository;
import io.mosip.gateway.payment.repository.PrnConsumedRepository;
import io.mosip.gateway.payment.repository.PrnTaxHeadRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * This service class handles are operation in regards to a PRN verification and
 * consumption
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

	@Autowired
	private PrnTaxHeadRepository prnTaxHeadRepository;

	@Autowired
	private PayableServiceTypeRepository payableServiceTypeRepository;
	
	@Autowired
	private PmsServiceRepository pmsServiceRepository;

	@Autowired
	private UraPaymentClient uraPaymentClient;

	@Value("${mosip.all.version}")
	private double version;

	@Value("${mosip.utc-datetime-pattern}")
	private String mosipDateTimeFormat;

	private final String createdBySystem = "SYSTEM";

	@Autowired
	private ObjectMapper objectMapper;
	
	public static final String SUCCESS_CODE = "200";

	@Value("${ura.foreign-currency.code}")
	private String uraForeignCurrencyCode;

	/**
	 * Creates a standard MOSIP response wrapper with version and timestamp.
	 */
	private <T> MainMosipResponseDTO<T> createResponse() {

		MainMosipResponseDTO<T> response = new MainMosipResponseDTO<>();

		response.setVersion(String.valueOf(version));
		response.setResponsetime(
				DateTimeFormatter.ofPattern(mosipDateTimeFormat)
						.format(LocalDateTime.now()));

		return response;
	}

	private boolean isInvalidPrn(String prn) {
		return prn == null || prn.trim().isEmpty();
	}

	
	private CheckPRNStatusResultDTO mapUraStatus(Object uraData) throws IOException {

		org.json.JSONObject jsonObject =
				new org.json.JSONObject(objectMapper.writeValueAsString(uraData));

		jsonObject.put("processFlowPaidFor",
				getMosipProcessForTaxHeadCode(jsonObject.get("taxHeadCode").toString()));

		jsonObject.put("subServiceTypePaidFor",
				getServiceTypeForTaxHeadCode(jsonObject.get("taxHeadCode").toString()));

		return objectMapper.readValue(
				jsonObject.toString(),
				CheckPRNStatusResultDTO.class);
	}

	/**
	 * Retrieves the status of a PRN.
	 * Checks the local database first and calls URA if the PRN is not cached
	 * or not yet validated.
	 */
	public MainMosipResponseDTO<CheckPRNStatusResultDTO> getPrnStatus(
			CheckPRNStatusRequestDTO prnStatusRequestDTO) throws Exception {

		log.info(AppConstants.STAGE_CHECK_PRN_STATUS + ":: getPrnStatus()");

		MainMosipResponseDTO<CheckPRNStatusResultDTO> response = createResponse();

		try {

			if (prnStatusRequestDTO != null) {

				if (!isInvalidPrn(prnStatusRequestDTO.getPrn())) {

					PrnConsumedEntity checkingAgainstPrnConsumedEntity =
							prnConsumedRepository.findByPrn(prnStatusRequestDTO.getPrn());

					PRNStatusDTO prnPaymentStatusDTO = null;
					CheckPRNStatusResponseDTO checkPRNStatusUraResponseDTO = null;

					if (checkingAgainstPrnConsumedEntity != null) {

						if (checkingAgainstPrnConsumedEntity.isPrnValid()) {

							log.info(AppConstants.STAGE_CHECK_PRN_STATUS + " -> "
									+ AppLogMessages.PRN_STATUS_CHECK_PRN_IN_DB_VALID.getMessage());

							prnPaymentStatusDTO =
									prepareResponseForPrnStatus(checkingAgainstPrnConsumedEntity, null);

						} else {

							log.info(AppConstants.STAGE_CHECK_PRN_STATUS + " -> "
									+ AppLogMessages.PRN_STATUS_CHECK_PRN_IN_DB_NOT_VALID.getMessage());

							checkPRNStatusUraResponseDTO =
									uraPaymentClient.checkPrnStatus(prnStatusRequestDTO);

							prnPaymentStatusDTO =
									prepareResponseForPrnStatus(
											checkingAgainstPrnConsumedEntity,
											checkPRNStatusUraResponseDTO);
						}

					} else {

						log.info(AppConstants.STAGE_CHECK_PRN_STATUS + " -> "
								+ AppLogMessages.PRN_STATUS_CHECK_PRN_NOT_IN_DB.getMessage());

						checkPRNStatusUraResponseDTO =
								uraPaymentClient.checkPrnStatus(prnStatusRequestDTO);

						prnPaymentStatusDTO =
								prepareResponseForPrnStatus(null, checkPRNStatusUraResponseDTO);
					}

					if (prnPaymentStatusDTO != null) {

						if (SUCCESS_CODE.equalsIgnoreCase(prnPaymentStatusDTO.getCode())) {

							response.setResponse(prnPaymentStatusDTO.getData());

						} else {

							ResponseUtil.setError(
									response,
									prnPaymentStatusDTO.getCode(),
									prnPaymentStatusDTO.getMessage());
						}
					}

				} else {

					ResponseUtil.setError(
							response,
							AppErrorMessages.NPG_PARAM_MISSING.getCode(),
							"PRN missing in request");

					log.error(AppErrorMessages.NPG_PARAM_MISSING.getCode() + " -> "
							+ AppErrorMessages.NPG_PARAM_MISSING.getMessage() + ": PRN");
				}

			} else {

				ResponseUtil.setError(response, AppErrorMessages.NPG_REQUEST_MISSING);

				log.error(AppErrorMessages.NPG_REQUEST_MISSING.getCode() + " -> "
						+ AppErrorMessages.NPG_REQUEST_MISSING.getMessage());
			}

		} catch (Exception e) {

			ResponseUtil.setError(response, AppErrorMessages.NPG_UNKNOWN_EXCEPTION);

			log.error(AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getCode() + " -> "
					+ AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getMessage() + ": " + e.getMessage());

			log.error(AppErrorMessages.PRN_CHECK_STATUS_FAILED.getCode() + " -> "
					+ AppErrorMessages.PRN_CHECK_STATUS_FAILED.getMessage() + ": " + e.getMessage());
		}

		return response;
	}
	
	/**
	 * Prepares the final PRN status response using either cached database data
	 * or the response returned from URA, updating the database when necessary.
	 */
	private PRNStatusDTO prepareResponseForPrnStatus(
			PrnConsumedEntity prnConsumedEntity,
			CheckPRNStatusResponseDTO checkPRNStatusUraResponseDTO)
			throws JSONException, IOException {

		PRNStatusDTO prnPaymentStatusDTO = new PRNStatusDTO();

		if (prnConsumedEntity != null && checkPRNStatusUraResponseDTO == null) {

			prnPaymentStatusDTO.setCode("200");
			prnPaymentStatusDTO.setMessage("Operation Successful - PRN Paid, Proceed");

			CheckPRNStatusResultDTO convertedObject =
					objectMapper.readValue(
							prnConsumedEntity.getPrnData(),
							CheckPRNStatusResultDTO.class);

			prnPaymentStatusDTO.setData(convertedObject);

		}
		else if (checkPRNStatusUraResponseDTO != null && prnConsumedEntity != null) {

			prnPaymentStatusDTO.setCode(checkPRNStatusUraResponseDTO.getCode());
			prnPaymentStatusDTO.setMessage(checkPRNStatusUraResponseDTO.getMessage());

			if (checkPRNStatusUraResponseDTO.getData() != null) {

				CheckPRNStatusResultDTO convertedObject =
						mapUraStatus(checkPRNStatusUraResponseDTO.getData());

				prnPaymentStatusDTO.setData(convertedObject);

				updatePrnConsumedEntity(
						prnConsumedEntity,
						objectMapper.writeValueAsString(convertedObject));
			}
		}
		else if (checkPRNStatusUraResponseDTO != null) {

			prnPaymentStatusDTO.setCode(checkPRNStatusUraResponseDTO.getCode());
			prnPaymentStatusDTO.setMessage(checkPRNStatusUraResponseDTO.getMessage());

			if (checkPRNStatusUraResponseDTO.getData() != null) {

				CheckPRNStatusResultDTO convertedObject =
						mapUraStatus(checkPRNStatusUraResponseDTO.getData());

				prnPaymentStatusDTO.setData(convertedObject);

				saveNewPrnConsumedEntity(
						objectMapper.writeValueAsString(convertedObject),
						convertedObject.getPrn());
			}
		}

		return prnPaymentStatusDTO;
	}

	private String getServiceTypeForTaxHeadCode(String taxHeadCode) {
		return payableServiceTypeRepository
				.findDistinctServiceTypeByTaxHeadCode(taxHeadCode);
	}

	private String getMosipProcessForTaxHeadCode(String taxHeadCode) {
		return payableServiceTypeRepository
				.findMosipProcessByTaxHeadCode(taxHeadCode);
	}

	private void updatePrnConsumedEntity(
			PrnConsumedEntity entity,
			String jsonData) throws IOException {

		JsonNode jsonNode = objectMapper.readTree(jsonData);

		entity.setPrnData(jsonData);

		entity.setPrnValid(
				jsonNode.get("statusCode").asText()
						.equals(PrnStatusCode.PRN_STATUS_RECEIVED_CREDITED.getStatusCode()));

		entity.setUpBy(createdBySystem);
		entity.setUpdDatetime(LocalDateTime.now());

		prnConsumedRepository.save(entity);
	}

	private void saveNewPrnConsumedEntity(String jsonData, String prn) {

		PrnConsumedEntity newConsumedEntity = new PrnConsumedEntity();

		newConsumedEntity.setId(UUID.randomUUID().toString());
		newConsumedEntity.setPrn(prn);
		newConsumedEntity.setPrnData(jsonData);
		newConsumedEntity.setCrBy(createdBySystem);
		newConsumedEntity.setCrDatetime(LocalDateTime.now());

		prnConsumedRepository.save(newConsumedEntity);
	}
	
	/**
	 * Retrieves PRN status specifically for PMS services and verifies
	 * whether the returned tax head is configured as a valid PMS service.
	 */
	public MainMosipResponseDTO<CheckPRNStatusPMSResultDTO> getPrnStatusPMS(
	        CheckPRNStatusRequestDTO requestDTO) {

	    log.info(AppConstants.STAGE_CHECK_PRN_STATUS + ":: getPrnStatusPMS()");

	    MainMosipResponseDTO<CheckPRNStatusPMSResultDTO> response = createResponse();

	    try {
	        if (requestDTO == null || requestDTO.getPrn() == null ||
	            requestDTO.getPrn().trim().isEmpty()) {
	        	ResponseUtil.setError(
				        response,
				        AppErrorMessages.NPG_PARAM_MISSING.getCode(),
				        "PRN missing in request"
				);
	            return response;
	        }

	        // ALWAYS call URA (no DB cache)       
	        CheckPRNStatusResponseDTO uraResponse =
	                uraPaymentClient.checkPrnStatus(requestDTO);

	        if (!"200".equalsIgnoreCase(uraResponse.getCode())) {
	            ResponseUtil.setError(response, uraResponse.getCode(),uraResponse.getMessage());
	            return response;
	        }

	        URACheckPRNStatusResultDTO uraData =
	                (URACheckPRNStatusResultDTO) uraResponse.getData();

	        org.json.JSONObject json =
	                new org.json.JSONObject(objectMapper.writeValueAsString(uraData));

	        boolean validPmsTaxHead =
	                pmsServiceRepository.existsActiveServiceForTaxHead(
	                        json.getString("taxHeadCode")
	                );

	        json.put("isValidPmsTaxHead", validPmsTaxHead);

	        CheckPRNStatusPMSResultDTO result =
	        		objectMapper.readValue(json.toString(),
	                		CheckPRNStatusPMSResultDTO.class);

	        response.setResponse(result);
	        return response;

	    } catch (Exception e) {
	        log.error(AppErrorMessages.PRN_CHECK_STATUS_FAILED.getMessage(), e);
	        ResponseUtil.setError(response, AppErrorMessages.PRN_CHECK_STATUS_FAILED);
	        return response;
	    }
	}


	/**
	 * Generates a PRN for a MOSIP service by resolving the service type,
	 * retrieving the tax head configuration, and calling URA.
	 */
	public MainMosipResponseDTO<PRNGeneratedDTO> generatePrn(GeneratePRNRequestDTO requestDTO) {

		log.info(AppConstants.STAGE_GENERATE_PRN + ":: generatePrn()");

		MainMosipResponseDTO<PRNGeneratedDTO> response = createResponse();
		GeneratePRNResponseDTO generatePRNResponseDTO = new GeneratePRNResponseDTO();
		PRNGeneratedDTO prnGeneratedDTO = new PRNGeneratedDTO();

		try {
			if (requestDTO != null && requestDTO.getService() != null && requestDTO.getFullName() != null
					&& !requestDTO.getService().isEmpty() && !requestDTO.getFullName().isEmpty()) {

				PayableServiceTypeEntity existingService = payableServiceTypeRepository
						.findByServiceTypeCode(requestDTO.getService()); // if service type uses Code from dynamic
																			// fields

				if (existingService != null) {
					PrnTaxHeadEntity existingTaxHead = prnTaxHeadRepository
							.findByTaxHeadCode(existingService.getPrnTaxHeadCode().getTaxHeadCode());

					if (existingTaxHead != null) {
						generatePRNResponseDTO =
					                uraPaymentClient.generatePrn(requestDTO, existingTaxHead, null);

						prnGeneratedDTO.setCode(generatePRNResponseDTO.getCode());
						prnGeneratedDTO.setMessage(generatePRNResponseDTO.getMessage());

						if (generatePRNResponseDTO.getData() != null) {

							// Check if the data is of type URAGetPRNResultDTO
							if (generatePRNResponseDTO.getData() instanceof URAGetPRNResultDTO) {
								URAGetPRNResultDTO uraData = (URAGetPRNResultDTO) generatePRNResponseDTO.getData();

								// Manually convert URAGetPRNResultDTO to GeneratePRNResultDTO
								GeneratePRNResultDTO convertedObject = new GeneratePRNResultDTO();
								convertedObject.setErrorCode(uraData.getErrorCode());
								convertedObject.setErrorDesc(uraData.getErrorDesc());
								convertedObject.setExpiryDate(uraData.getExpiryDate());
								convertedObject.setPrn(uraData.getPrn());
								convertedObject.setSearchCode(uraData.getSearchCode());
								convertedObject.setAmount(existingTaxHead.getTaxHeadAmount());
								convertedObject.setCurrency(existingTaxHead.getCurrency());
								
								prnGeneratedDTO.setData(convertedObject);
							} else {
								log.warn("Unexpected data type: {}",
										generatePRNResponseDTO.getData().getClass().getName());
								prnGeneratedDTO.setData(null);
							}
						} else {
							prnGeneratedDTO.setData(null);
						}

						// Update the response based on the payment status code
						if (SUCCESS_CODE.equalsIgnoreCase(generatePRNResponseDTO.getCode())) {
							response.setResponse(prnGeneratedDTO);
						} else {

							
							ResponseUtil.setError(response, generatePRNResponseDTO.getCode(),generatePRNResponseDTO.getMessage());
						}
					} else {

						ResponseUtil.setError(response, AppErrorMessages.NPG_TAXHEAD_NOT_FOUND_FOR_SERVICE);
						
						log.error(AppErrorMessages.NPG_TAXHEAD_NOT_FOUND_FOR_SERVICE.getCode() + " -> "
								+ AppErrorMessages.NPG_TAXHEAD_NOT_FOUND_FOR_SERVICE.getMessage());

					}
				} else {

					ResponseUtil.setError(response, AppErrorMessages.PAYABLE_SERVICE_TYPE_NOT_FOUND);
					
					log.error(AppErrorMessages.PAYABLE_SERVICE_TYPE_NOT_FOUND.getCode() + " -> "
							+ AppErrorMessages.PAYABLE_SERVICE_TYPE_NOT_FOUND.getMessage());
				}
			} else {
				ResponseUtil.setError(response, AppErrorMessages.NPG_REQUEST_MISSING);
				log.error(AppErrorMessages.NPG_REQUEST_MISSING.getCode() + " -> "
						+ AppErrorMessages.NPG_REQUEST_MISSING.getMessage());
			}

		} catch (Exception e) {
			ResponseUtil.setError(response, AppErrorMessages.NPG_UNKNOWN_EXCEPTION);
			log.error(AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getCode() + " -> "
					+ AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getMessage() + ": " + e.getMessage());

			log.error(AppErrorMessages.PRN_GENERATION_FAILED.getCode() + " -> "
					+ AppErrorMessages.PRN_GENERATION_FAILED.getMessage() + ": " + e.getMessage());

		}

		return response;
	}
	
	/**
	 * Generates a PRN for PMS services. The total amount is calculated based
	 * on the configured tax head amount and the number of records submitted.
	 */
	public MainMosipResponseDTO<GeneratePrnPMSResultDTO> generatePrnPMS(
	        GeneratePrnPMSRequestDTO requestDTO) {

	    log.info(AppConstants.STAGE_GENERATE_PRN + ":: generatePrnPMS()");

	    MainMosipResponseDTO<GeneratePrnPMSResultDTO> response = createResponse();


	    try {

	        if (requestDTO == null ||
	                requestDTO.getPartnerName() == null ||
	                requestDTO.getPartnerType() == null ||
	                requestDTO.getPartnerGroup() == null ||
	                requestDTO.getNumberOfRecords() == null ||
	                requestDTO.getNumberOfRecords() <= 0) {
	        	
	        	ResponseUtil.setError(
				        response,
				        AppErrorMessages.NPG_PARAM_MISSING.getCode(),
				        "Invalid PMS PRN request"
				);
	            return response;
	        }

	        Optional<PmsServiceEntity> optionalService =
	                pmsServiceRepository.findActiveService(
	                        requestDTO.getPartnerType(),
	                        requestDTO.getPartnerGroup()
	                );

	        if (optionalService.isEmpty()) {

	            ResponseUtil.setError(
	                    response,
	                    AppErrorMessages.PARTNER_TYPE_GROUP_NOT_FOR_PMS
	            );
	            
	            
	            return response;
	        }

	        PmsServiceEntity pmsService = optionalService.get();
	        PrnTaxHeadEntity taxHeadEntity = pmsService.getPrnTaxHeadCode();

	        Double taxHeadAmount =
	                Double.parseDouble(taxHeadEntity.getTaxHeadAmount());

	        Double totalAmount =
	                taxHeadAmount * requestDTO.getNumberOfRecords();

	        GeneratePRNRequestDTO internalRequest =
	                new GeneratePRNRequestDTO();

	        internalRequest.setFullName(requestDTO.getPartnerName());
	        
	        log.info("PMS PRN Request -> PartnerName: {}, PartnerType: {}, PartnerGroup: {}, "
	                + "TaxHead: {}, Records: {}, Amount: {}, Currency: {}",
	                requestDTO.getPartnerName(),
	                requestDTO.getPartnerType(),
	                requestDTO.getPartnerGroup(),
	                taxHeadEntity.getTaxHeadCode(),
	                requestDTO.getNumberOfRecords(),
	                totalAmount,
	                taxHeadEntity.getCurrency());

	        GeneratePRNResponseDTO generateResponse =
	                uraPaymentClient.generatePrn(internalRequest, taxHeadEntity, totalAmount);

	        if (SUCCESS_CODE.equalsIgnoreCase(generateResponse.getCode())) {

	            if (generateResponse.getData() instanceof URAGetPRNResultDTO) {

	                URAGetPRNResultDTO uraData =
	                        (URAGetPRNResultDTO) generateResponse.getData();

	                GeneratePrnPMSResultDTO result =
	                        new GeneratePrnPMSResultDTO();

	                result.setPrn(uraData.getPrn());
	                result.setExpiryDate(uraData.getExpiryDate());
	                result.setCurrency(taxHeadEntity.getCurrency());
	                result.setAmount(totalAmount);
	                result.setNumberOfRecords(requestDTO.getNumberOfRecords());

	                response.setResponse(result);

	            } else {
	            	
	            	 ResponseUtil.setError(
	 	                    response,
	 	                    "PMS002",
	 	                    "Unexpected URA response format"
	 	            );
	 	            
	            }

	        } else {
	            ResponseUtil.setError(
 	                    response,
 	                   generateResponse.getCode(),
 	                  generateResponse.getMessage()
 	            );
	            
	            
	        }

	    } catch (Exception e) {

	        log.error(AppErrorMessages.PRN_GENERATION_FAILED.getCode() + " -> "
	                + AppErrorMessages.PRN_GENERATION_FAILED.getMessage(), e);
	        
	        ResponseUtil.setError(response, AppErrorMessages.PRN_GENERATION_FAILED);
	    }

	    return response;
	}


	/**
	 * Marks a PRN as consumed by linking it to a registration ID
	 * and storing the transaction in the database.
	 */
	public MainMosipResponseDTO<ConsumePRNResponseDTO> consumePrnAsUsed(ConsumePRNRequestDTO requestDTO) {

		log.info(AppConstants.STAGE_CONSUME_PRN_AS_USED + ":: consumePrnAsUsed()");

		MainMosipResponseDTO<ConsumePRNResponseDTO> response = createResponse();
		ConsumePRNResponseDTO consumePrnResponseDTO = new ConsumePRNResponseDTO();

		try {

			if (requestDTO != null && requestDTO.getPrn() != null && requestDTO.getRegId() != null
					&& !requestDTO.getPrn().isEmpty() && !requestDTO.getRegId().isEmpty()) {

				consumePrnResponseDTO.setPrnNum(requestDTO.getPrn());

				PrnTransactionEntity existingPrnTransactionEntity = prnTransactionLogRepository
						.findByPrn(requestDTO.getPrn());

				if (existingPrnTransactionEntity != null) {
					consumePrnResponseDTO.setRegIdTaggedToPrn(existingPrnTransactionEntity.getRegId());
					consumePrnResponseDTO.setConsumedSucess(false);
					response.setResponse(consumePrnResponseDTO);
					
					ResponseUtil.setError(response, AppErrorMessages.PRN_ALREADY_CONSUMED);
					
					
					log.error(AppErrorMessages.PRN_ALREADY_CONSUMED.getCode() + " -> "
							+ AppErrorMessages.PRN_ALREADY_CONSUMED.getMessage());
				} else {
					/* save to db for prn_transaction_logs */
					PrnTransactionEntity prnTransactionLogEntity = new PrnTransactionEntity();
					prnTransactionLogEntity.setId(UUID.randomUUID().toString());
					prnTransactionLogEntity.setPrn(requestDTO.getPrn());
					prnTransactionLogEntity.setRegId(requestDTO.getRegId());
					prnTransactionLogEntity.setCrBy(createdBySystem);
					prnTransactionLogEntity.setCrDatetime(LocalDateTime.now());
					prnTransactionLogRepository.save(prnTransactionLogEntity);

					log.info(AppSuccessMessages.PRN_CONSUMPTION_SUCCESS.getMessage());
					consumePrnResponseDTO.setConsumedSucess(true);
					consumePrnResponseDTO.setRegIdTaggedToPrn(requestDTO.getRegId());
					response.setResponse(consumePrnResponseDTO);
				}
			} else {

				ResponseUtil.setError(response, AppErrorMessages.NPG_REQUEST_MISSING);
				log.error(AppErrorMessages.NPG_REQUEST_MISSING.getCode() + " -> "
						+ AppErrorMessages.NPG_REQUEST_MISSING.getMessage());
			}

		} catch (Exception e) {
			ResponseUtil.setError(response, AppErrorMessages.NPG_UNKNOWN_EXCEPTION);
			log.error(AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getCode() + " -> "
					+ AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getMessage() + ": " + e.getMessage());

			log.error(AppErrorMessages.PRN_CONSUMPTION_FAILED.getCode() + " -> "
					+ AppErrorMessages.PRN_CONSUMPTION_FAILED.getMessage() + ": " + e.getMessage());
		}

		return response;
	}

	/**
	 * Checks whether a PRN already exists in the transaction logs
	 * and returns the registration ID it was associated with.
	 */
	public MainMosipResponseDTO<IsPRNRegInLogsResponseDTO> checkIfPrnAndRegIdPresentInLogs(
			IsPRNRegInLogsRequestDTO isPrnRegInLogsRequestDTO) {

		log.info(AppConstants.STAGE_CONSUME_PRN_AS_USED + ":: checkIfPrnAndRegIdPresentInLogs()");

		MainMosipResponseDTO<IsPRNRegInLogsResponseDTO> response = createResponse();
		IsPRNRegInLogsResponseDTO isPrnRegInLogsResponseDTO = new IsPRNRegInLogsResponseDTO();

		try {

			if (isPrnRegInLogsRequestDTO != null && isPrnRegInLogsRequestDTO.getPrn() != null
					&& !isPrnRegInLogsRequestDTO.getPrn().trim().isEmpty()) {
				PrnTransactionEntity prnTransactionLogEntity = prnTransactionLogRepository
						.findByPrn(isPrnRegInLogsRequestDTO.getPrn());

				if (prnTransactionLogEntity != null) {

					isPrnRegInLogsResponseDTO.setPrn(isPrnRegInLogsRequestDTO.getPrn());
					isPrnRegInLogsResponseDTO.setRegIdTagged(prnTransactionLogEntity.getRegId());
					isPrnRegInLogsResponseDTO.setPresentInLogs(true);
					ResponseUtil.setError(response, AppErrorMessages.PRN_ALREADY_CONSUMED_BY_DIFFERENT_REGID.getCode(),
							"PRN already consumed by Reg Id: " + prnTransactionLogEntity.getRegId());
					log.info(AppErrorMessages.PRN_ALREADY_CONSUMED_BY_DIFFERENT_REGID.getCode() + " -> "
							+ AppErrorMessages.PRN_ALREADY_CONSUMED_BY_DIFFERENT_REGID.getMessage());
				} else {
					isPrnRegInLogsResponseDTO.setPrn(isPrnRegInLogsRequestDTO.getPrn());
					isPrnRegInLogsResponseDTO.setPresentInLogs(false);
				}
				response.setResponse(isPrnRegInLogsResponseDTO);
			} else {

				ResponseUtil.setError(response, AppErrorMessages.NPG_REQUEST_MISSING);
				log.error(AppErrorMessages.NPG_REQUEST_MISSING.getCode() + " -> "
						+ AppErrorMessages.NPG_REQUEST_MISSING.getMessage());
			}
		} catch (Exception e) {
			ResponseUtil.setError(response, AppErrorMessages.NPG_UNKNOWN_EXCEPTION);
			log.error(AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getCode() + " -> "
					+ AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getMessage() + ": " + e.getMessage());

		}
		return response;
	}

	/**
	 * Returns all stored PRNs with their status information for
	 * administrative or auditing purposes.
	 */
	public MainMosipResponseDTO<PrnsConsumedListMetaDTO> findAllConsumedPrns() {

		log.info(AppConstants.STAGE_CONSUME_PRN_AS_USED + ":: findAllConsumedPrns()");
		MainMosipResponseDTO<PrnsConsumedListMetaDTO> response = createResponse();
		PrnsConsumedListMetaDTO prnsListMetaDTO = new PrnsConsumedListMetaDTO();

		try {

			List<PrnConsumedEntity> listPrns = prnConsumedRepository.findAll();

			if (listPrns != null) {

				List<PrnsConsumedListViewDTO> viewList = new ArrayList<>();

				for (PrnConsumedEntity prnTransactionEntity : listPrns) {
					PrnsConsumedListViewDTO viewDto = new PrnsConsumedListViewDTO();
					viewDto.setPrn(prnTransactionEntity.getPrn());

					CheckPRNStatusResultDTO convertedObject = objectMapper.readValue(prnTransactionEntity.getPrnData(),
							new TypeReference<CheckPRNStatusResultDTO>() {
							});
					viewDto.setPrnData(convertedObject);
					viewDto.setPrnValid(prnTransactionEntity.isPrnValid());

					viewDto.setDateCreated(prnTransactionEntity.getCrDatetime());
					viewDto.setDateUpdated(prnTransactionEntity.getUpdDatetime());
					viewList.add(viewDto);
				}

				prnsListMetaDTO.setPrns(viewList);
				prnsListMetaDTO.setTotalRecords(Integer.toString(listPrns.size()));
				response.setResponse(prnsListMetaDTO);
			} else {
				ResponseUtil.setError(response, AppErrorMessages.NPG_ENTITIES_NOT_FOUND.getCode(),"No PRNs found" );
				
				log.info(AppErrorMessages.NPG_ENTITIES_NOT_FOUND.getCode() + " -> "
						+ AppErrorMessages.NPG_ENTITIES_NOT_FOUND.getMessage() + ": PRN");

			}
		} catch (Exception ex) {
			ResponseUtil.setError(response, AppErrorMessages.NPG_UNKNOWN_EXCEPTION);
			log.error(AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getCode() + " -> "
					+ AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getMessage() + ": " + ex.getMessage());


		}
		return response;
	}
}
