package io.mosip.gateway.payment.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.gateway.payment.constants.AppConstants;
import io.mosip.gateway.payment.constants.AppErrorMessages;
import io.mosip.gateway.payment.constants.AppLogMessages;
import io.mosip.gateway.payment.constants.AppSuccessMessages;
import io.mosip.gateway.payment.constants.PrnStatusCode;
import io.mosip.gateway.payment.constants.TaxHeadCode;
import io.mosip.gateway.payment.dto.request.CheckPRNStatusRequestDTO;
import io.mosip.gateway.payment.dto.request.CheckPRNStatusResultDTO;
import io.mosip.gateway.payment.dto.request.ConsumePRNRequestDTO;
import io.mosip.gateway.payment.dto.request.GeneratePRNRequestDTO;
import io.mosip.gateway.payment.dto.request.IsPRNRegInLogsRequestDTO;
import io.mosip.gateway.payment.dto.response.CheckPRNStatusResponseDTO;
import io.mosip.gateway.payment.dto.response.ConsumePRNResponseDTO;
import io.mosip.gateway.payment.dto.response.ExceptionJSONInfoDTO;
import io.mosip.gateway.payment.dto.response.GeneratePRNResponseDTO;
import io.mosip.gateway.payment.dto.response.GeneratePRNResultDTO;
import io.mosip.gateway.payment.dto.response.IsPRNRegInLogsResponseDTO;
import io.mosip.gateway.payment.dto.response.MainMosipResponseDTO;
import io.mosip.gateway.payment.dto.response.PRNGeneratedDTO;
import io.mosip.gateway.payment.dto.response.PRNStatusDTO;
import io.mosip.gateway.payment.dto.response.PrnsConsumedListMetaDTO;
import io.mosip.gateway.payment.dto.response.PrnsConsumedListViewDTO;
import io.mosip.gateway.payment.dto.ura.URACheckPRNStatusResultDTO;
import io.mosip.gateway.payment.dto.ura.URAGetPRNResultDTO;
import io.mosip.gateway.payment.dto.ura.URASoapCheckPRNStatusRequestDTO;
import io.mosip.gateway.payment.dto.ura.URASoapGeneratePRNRequestDTO;
import io.mosip.gateway.payment.entity.PayableServiceTypeEntity;
import io.mosip.gateway.payment.entity.PrnConsumedEntity;
import io.mosip.gateway.payment.entity.PrnTaxHeadEntity;
import io.mosip.gateway.payment.entity.PrnTransactionEntity;
import io.mosip.gateway.payment.repository.PrnTransactionRepository;
import io.mosip.gateway.payment.util.URASoapServiceUtil;
import io.mosip.gateway.payment.repository.PayableServiceTypeRepository;
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
	private URASoapServiceUtil soapServiceUtil;

	@Value("${mosip.all.version}")
	private double version;

	@Value("${mosip.utc-datetime-pattern}")
	private String mosipDateTimeFormat;

	private final String createdBySystem = "SYSTEM";

	ObjectMapper objectMapper;

	@Autowired
	RestTemplate restTemplate;

	@Value("${ura.user}")
	private String mdaUsername;

	@Value("${ura.key}")
	private String mdaPass;

	@Value("${ura.signed}")
	private String signedCredentials;

	@Value("${ura.encrypted}")
	private String encryptedCredentials;

	@Value("${ura.wsdl}")
	private String uraWsdl;

	@Value("${ura.action.check-prn-status}")
	private String uraCheckPRNStatusAction;

	@Value("${ura.action.get-prn}")
	private String uraGetPRNAction;

	public MainMosipResponseDTO<CheckPRNStatusResultDTO> getPrnStatus(CheckPRNStatusRequestDTO prnStatusRequestDTO)
			throws Exception {
		log.info(AppConstants.STAGE_CHECK_PRN_STATUS + ":: getPrnStatus()");

		MainMosipResponseDTO<CheckPRNStatusResultDTO> response = new MainMosipResponseDTO<>();

		response.setVersion(String.valueOf(version));
		response.setResponsetime(DateTimeFormatter.ofPattern(mosipDateTimeFormat).format(LocalDateTime.now()));

		List<ExceptionJSONInfoDTO> explist = new ArrayList<>();
		ExceptionJSONInfoDTO exception = new ExceptionJSONInfoDTO();

		try {
			if (!Objects.isNull(prnStatusRequestDTO)) {
				if (prnStatusRequestDTO.getPrn() != null && !prnStatusRequestDTO.getPrn().trim().isEmpty()) {

					PrnConsumedEntity checkingAgainstPrnConsumedEntity = prnConsumedRepository
							.findByPrn(prnStatusRequestDTO.getPrn());

					PRNStatusDTO prnPaymentStatusDTO = null;
					CheckPRNStatusResponseDTO checkPRNStatusUraResponseDTO = null;

					if (!Objects.isNull(checkingAgainstPrnConsumedEntity)) {
						if (checkingAgainstPrnConsumedEntity.isPrnValid()) {
							log.info(AppConstants.STAGE_CHECK_PRN_STATUS + " -> "
									+ AppLogMessages.PRN_STATUS_CHECK_PRN_IN_DB_VALID.getMessage());
							prnPaymentStatusDTO = prepareResponseForPrnStatus(checkingAgainstPrnConsumedEntity, null);
						} else {
							log.info(AppConstants.STAGE_CHECK_PRN_STATUS + " -> "
									+ AppLogMessages.PRN_STATUS_CHECK_PRN_IN_DB_NOT_VALID.getMessage());
							checkPRNStatusUraResponseDTO = checkPrnStatusURASOAP(prnStatusRequestDTO);
							prnPaymentStatusDTO = prepareResponseForPrnStatus(checkingAgainstPrnConsumedEntity,
									checkPRNStatusUraResponseDTO);
						}
					} else {
						log.info(AppConstants.STAGE_CHECK_PRN_STATUS + " -> "
								+ AppLogMessages.PRN_STATUS_CHECK_PRN_NOT_IN_DB.getMessage());
						checkPRNStatusUraResponseDTO = checkPrnStatusURASOAP(prnStatusRequestDTO);
						prnPaymentStatusDTO = prepareResponseForPrnStatus(null, checkPRNStatusUraResponseDTO);
					}

					// Update the response based on the payment status code
					if (prnPaymentStatusDTO != null) {
						if (prnPaymentStatusDTO.getCode().equalsIgnoreCase("200")) {
							response.setResponse(prnPaymentStatusDTO.getData());
						} else {
							exception.setMessage(prnPaymentStatusDTO.getMessage());
							exception.setErrorCode(prnPaymentStatusDTO.getCode());
							explist.add(exception);
							response.setErrors(explist);
						}
					}
				} else {
					exception.setMessage("Bad request. Request {} missing. Check PRN");
					explist.add(exception);
					response.setErrors(explist);
					log.error(AppErrorMessages.NPG_PARAM_MISSING.getCode() + " -> "
							+ AppErrorMessages.NPG_PARAM_MISSING.getMessage() + ": PRN");
				}
			} else {
				exception.setMessage("Bad request. Request {} missing.");
				explist.add(exception);
				response.setErrors(explist);
				log.error(AppErrorMessages.NPG_REQUEST_MISSING.getCode() + " -> "
						+ AppErrorMessages.NPG_REQUEST_MISSING.getMessage());
			}
		} catch (Exception e) {
			// Handle unexpected exceptions
			exception.setMessage(AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getMessage());
			exception.setErrorCode(AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getCode());
			log.error(AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getCode() + " -> "
					+ AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getMessage() + ": " + e.getMessage());
			explist.add(exception);
			response.setErrors(explist);
			log.error(AppErrorMessages.PRN_CHECK_STATUS_FAILED.getCode() + " -> "
					+ AppErrorMessages.PRN_CHECK_STATUS_FAILED.getMessage() + ": " + e.getMessage());
		}
		return response;
	}

	private PRNStatusDTO prepareResponseForPrnStatus(PrnConsumedEntity prnConsumedEntity,
			CheckPRNStatusResponseDTO checkPRNStatusUraResponseDTO) throws JSONException, IOException {

		PRNStatusDTO prnPaymentStatusDTO = new PRNStatusDTO();
		ObjectMapper objectMapper = new ObjectMapper();

		// If PRN exists in DB and is valid
		if (prnConsumedEntity != null && checkPRNStatusUraResponseDTO == null) {
			prnPaymentStatusDTO.setCode("200");
			prnPaymentStatusDTO.setMessage("Operation Successful - PRN Paid, Proceed");
			CheckPRNStatusResultDTO convertedObject = objectMapper.readValue(prnConsumedEntity.getPrnData(),
					CheckPRNStatusResultDTO.class);
			prnPaymentStatusDTO.setData(convertedObject);
		}
		// If PRN exists in DB but not valid
		else if (checkPRNStatusUraResponseDTO != null && prnConsumedEntity != null) {
			prnPaymentStatusDTO.setCode(checkPRNStatusUraResponseDTO.getCode());
			prnPaymentStatusDTO.setMessage(checkPRNStatusUraResponseDTO.getMessage());

			if (checkPRNStatusUraResponseDTO.getData() != null) {

				org.json.JSONObject jsonObject = new org.json.JSONObject(
						objectMapper.writeValueAsString(checkPRNStatusUraResponseDTO.getData()));
				
				jsonObject.put("processFlowPaidFor",
						getMosipProcessForTaxHeadCode(jsonObject.get("taxHeadCode").toString()));
				
				jsonObject.put("subServiceTypePaidFor",
						getServiceTypeForTaxHeadCode(jsonObject.get("taxHeadCode").toString()));

				CheckPRNStatusResultDTO convertedObject = objectMapper.readValue(jsonObject.toString(),
						CheckPRNStatusResultDTO.class);

				prnPaymentStatusDTO.setData(convertedObject);
				updatePrnConsumedEntity(prnConsumedEntity, jsonObject.toString());
			}
		}
		// If PRN doesn't exist in DB - Contact URA API
		else if (checkPRNStatusUraResponseDTO != null) {
			prnPaymentStatusDTO.setCode(checkPRNStatusUraResponseDTO.getCode());
			prnPaymentStatusDTO.setMessage(checkPRNStatusUraResponseDTO.getMessage());

			if (checkPRNStatusUraResponseDTO.getData() != null) {

				org.json.JSONObject jsonObject = new org.json.JSONObject(
						objectMapper.writeValueAsString(checkPRNStatusUraResponseDTO.getData()));

				jsonObject.put("processFlowPaidFor",
						getMosipProcessForTaxHeadCode(jsonObject.get("taxHeadCode").toString()));
				
				jsonObject.put("subServiceTypePaidFor",
						getServiceTypeForTaxHeadCode(jsonObject.get("taxHeadCode").toString()));

				CheckPRNStatusResultDTO convertedObject = objectMapper.readValue(jsonObject.toString(),
						CheckPRNStatusResultDTO.class);

				prnPaymentStatusDTO.setData(convertedObject);

				String prn = convertedObject.getPrn();
				saveNewPrnConsumedEntity(jsonObject.toString(), prn);
			} else {
				prnPaymentStatusDTO.setData(null);
			}
		}
		return prnPaymentStatusDTO;
	}

	/*
	 * private String getProcessFlowForResponse(String taxHeadCode) {
	 * 
	 * PrnTaxHeadEntity existingTaxHeadEntity =
	 * prnTaxHeadRepository.findByTaxHeadCode(taxHeadCode);
	 * 
	 * if(!Objects.isNull(existingTaxHeadEntity)) { return
	 * existingTaxHeadEntity.getMosipProcess(); } return null; }
	 */

	private String getServiceTypeForTaxHeadCode(String taxHeadCode) {
		return payableServiceTypeRepository.findDistinctServiceTypeByTaxHeadCode(taxHeadCode);
	}

	private String getMosipProcessForTaxHeadCode(String taxHeadCode) {
		return payableServiceTypeRepository.findMosipProcessByTaxHeadCode(taxHeadCode);
	}

	private void updatePrnConsumedEntity(PrnConsumedEntity entity, String jsonData) throws IOException {

		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode = objectMapper.readTree(jsonData);

		entity.setPrnData(jsonData);
		entity.setPrnValid(
				jsonNode.get("statusCode").asText().equals(PrnStatusCode.PRN_STATUS_RECEIVED_CREDITED.getStatusCode()));
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
	 * This method calls an external SOAP URA API Service to get the status of a PRN
	 * 
	 * @param requestDTO
	 * @return
	 */
	private CheckPRNStatusResponseDTO checkPrnStatusURASOAP(CheckPRNStatusRequestDTO requestDTO) {

		log.info(AppConstants.STAGE_CHECK_PRN_STATUS + ":: checkPrnStatusURASOAP()");

		CheckPRNStatusResponseDTO responseDTO = new CheckPRNStatusResponseDTO();

		try {
			// Prepare the request DTO
			URASoapCheckPRNStatusRequestDTO uraRequestDTO = new URASoapCheckPRNStatusRequestDTO();
			uraRequestDTO.setStrPRN(requestDTO.getPrn());
			uraRequestDTO.setUserName(mdaUsername);
			uraRequestDTO.setConcatenatedUsernamePasswordSignature(signedCredentials);
			uraRequestDTO.setEncryptedConcatenatedUsernamePassword(encryptedCredentials);

			// Call the SOAP service
			String response = soapServiceUtil.checkPRNStatus(uraRequestDTO);

			if (response != null) {
				log.info(AppSuccessMessages.SOAP_RESPONSE_SUCCESS.getMessage() + " -> " + response);

				// Parse the response
				URACheckPRNStatusResultDTO resultDTO = soapServiceUtil.parseCheckPRNStatusResponse(response);

				// Check for authentication error
				if (resultDTO.getErrorCode() != null && resultDTO.getErrorCode().equalsIgnoreCase("ERR01")) {
					log.error(AppErrorMessages.SOAP_AUTHENTICATION_ERROR.getCode() + " -> "
							+ AppErrorMessages.SOAP_AUTHENTICATION_ERROR.getMessage());
					responseDTO.setCode(AppErrorMessages.SOAP_AUTHENTICATION_ERROR.getCode());
					responseDTO.setMessage(AppErrorMessages.SOAP_AUTHENTICATION_ERROR.getMessage());
				} else {
					// Handle different error codes
					String errorCode = resultDTO.getErrorCode();

					switch (errorCode) {
					case "A": // Available
					case "D": // Received but dishonored
					case "R": // Received but not credited
					case "T": // Received and credited
						// Success codes
						responseDTO.setCode("200");
						responseDTO.setMessage(resultDTO.getStatusDesc());
						responseDTO.setData(resultDTO);
						break;

					case "N": // Invalid PRN
						log.error(AppErrorMessages.URA_PRN_INVALID.getCode() + " -> "
								+ AppErrorMessages.URA_PRN_INVALID.getMessage());
						responseDTO.setCode(AppErrorMessages.URA_PRN_INVALID.getCode());
						responseDTO.setMessage(AppErrorMessages.URA_PRN_INVALID.getMessage());
						break;

					case "C": // Cancelled
						log.error(AppErrorMessages.URA_PRN_CANCELLED.getCode() + " -> "
								+ AppErrorMessages.URA_PRN_CANCELLED.getMessage());
						responseDTO.setCode(AppErrorMessages.URA_PRN_CANCELLED.getCode());
						responseDTO.setMessage(AppErrorMessages.URA_PRN_CANCELLED.getMessage());
						break;

					case "X": // Expired
						log.error(AppErrorMessages.URA_PRN_CANCELLED.getCode() + " -> "
								+ AppErrorMessages.URA_PRN_CANCELLED.getMessage());
						responseDTO.setCode(AppErrorMessages.URA_PRN_CANCELLED.getCode());
						responseDTO.setMessage(AppErrorMessages.URA_PRN_CANCELLED.getMessage());
						break;

					case "DB001": // No Data Found
						log.error(AppErrorMessages.URA_PRN_NOT_FOUND.getCode() + " -> "
								+ AppErrorMessages.URA_PRN_NOT_FOUND.getMessage());
						responseDTO.setCode(AppErrorMessages.URA_PRN_NOT_FOUND.getCode());
						responseDTO.setMessage(AppErrorMessages.URA_PRN_NOT_FOUND.getMessage());
						break;

					case "PMT001": // PRN not found
						log.error(AppErrorMessages.URA_PRN_NOT_FOUND.getCode() + " -> "
								+ AppErrorMessages.URA_PRN_NOT_FOUND.getMessage());
						responseDTO.setCode(AppErrorMessages.URA_PRN_NOT_FOUND.getCode());
						responseDTO.setMessage(AppErrorMessages.URA_PRN_NOT_FOUND.getMessage());
						break;

					default:
						// Handle unexpected error codes
						log.error(AppErrorMessages.URA_UNEXPECTED_ERROR.getCode() + " -> "
								+ AppErrorMessages.URA_UNEXPECTED_ERROR.getMessage() + ": " + errorCode);
						responseDTO.setCode(resultDTO.getErrorCode());
						responseDTO.setMessage(resultDTO.getErrorDesc());
						break;
					}
				}
				return responseDTO;
			} else {
				log.error(AppErrorMessages.SOAP_RESPONSE_NULL.getCode() + " -> "
						+ AppErrorMessages.SOAP_RESPONSE_NULL.getMessage());
				responseDTO.setCode(AppErrorMessages.SOAP_RESPONSE_NULL.getCode());
				responseDTO.setMessage(AppErrorMessages.SOAP_RESPONSE_NULL.getMessage());
				return responseDTO;
			}

		} catch (Exception e) {
			log.error(AppErrorMessages.PRN_CHECK_STATUS_FAILED.getCode() + " -> "
					+ AppErrorMessages.PRN_CHECK_STATUS_FAILED.getMessage() + ": " + e.getMessage());
			responseDTO.setCode(AppErrorMessages.PRN_CHECK_STATUS_FAILED.getCode());
			responseDTO.setMessage(AppErrorMessages.PRN_CHECK_STATUS_FAILED.getMessage());
			return responseDTO;
		}
	}

	public MainMosipResponseDTO<PRNGeneratedDTO> generatePrn(GeneratePRNRequestDTO requestDTO) {

		log.info(AppConstants.STAGE_GENERATE_PRN + ":: generatePrn()");

		MainMosipResponseDTO<PRNGeneratedDTO> response = new MainMosipResponseDTO<PRNGeneratedDTO>();
		GeneratePRNResponseDTO generatePRNResponseDTO = new GeneratePRNResponseDTO();
		PRNGeneratedDTO prnGeneratedDTO = new PRNGeneratedDTO();

		response.setVersion(String.valueOf(version));
		response.setResponsetime(DateTimeFormatter.ofPattern(mosipDateTimeFormat).format(LocalDateTime.now()));

		List<ExceptionJSONInfoDTO> explist = new ArrayList<ExceptionJSONInfoDTO>();
		ExceptionJSONInfoDTO exception = new ExceptionJSONInfoDTO();

		try {
			if (!Objects.isNull(requestDTO) && requestDTO.getService() != null && requestDTO.getFullName() != null
					&& !requestDTO.getService().isEmpty() && !requestDTO.getFullName().isEmpty()) {

				/*
				 * PrnTaxHeadEntity existingTaxHeadProcess = prnTaxHeadRepository
				 * .findByMosipProcess(requestDTO.getService());
				 */

				PayableServiceTypeEntity existingService = payableServiceTypeRepository
						.findByServiceTypeCode(requestDTO.getService()); // if service type uses Code from dynamic
																			// fields

				if (!Objects.isNull(existingService)) {
					PrnTaxHeadEntity existingTaxHead = prnTaxHeadRepository
							.findByTaxHeadCode(existingService.getPrnTaxHeadCode().getTaxHeadCode());

					if (!Objects.isNull(existingTaxHead)) {
						generatePRNResponseDTO = getPrnURASOAP(requestDTO, existingTaxHead);

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
						if (generatePRNResponseDTO.getCode().equalsIgnoreCase("200")) {
							response.setResponse(prnGeneratedDTO);
						} else {

							exception.setMessage(generatePRNResponseDTO.getMessage());
							exception.setErrorCode(prnGeneratedDTO.getCode());
							explist.add(exception);
							response.setErrors(explist);
						}
					} else {
						exception.setMessage(AppErrorMessages.NPG_TAXHEAD_NOT_FOUND_FOR_SERVICE.getMessage());
						exception.setErrorCode(AppErrorMessages.NPG_TAXHEAD_NOT_FOUND_FOR_SERVICE.getCode());
						log.error(AppErrorMessages.NPG_TAXHEAD_NOT_FOUND_FOR_SERVICE.getCode() + " -> "
								+ AppErrorMessages.NPG_TAXHEAD_NOT_FOUND_FOR_SERVICE.getMessage());
						explist.add(exception);
						response.setErrors(explist);

					}
				} else {
					exception.setMessage(AppErrorMessages.PAYABLE_SERVICE_TYPE_NOT_FOUND.getMessage());
					exception.setErrorCode(AppErrorMessages.PAYABLE_SERVICE_TYPE_NOT_FOUND.getCode());
					log.error(AppErrorMessages.PAYABLE_SERVICE_TYPE_NOT_FOUND.getCode() + " -> "
							+ AppErrorMessages.PAYABLE_SERVICE_TYPE_NOT_FOUND.getMessage());
					explist.add(exception);
					response.setErrors(explist);
				}
			} else {
				exception.setMessage("Bad request. Request {} missing.");
				explist.add(exception);
				response.setErrors(explist);
				log.error(AppErrorMessages.NPG_REQUEST_MISSING.getCode() + " -> "
						+ AppErrorMessages.NPG_REQUEST_MISSING.getMessage());
			}

		} catch (Exception e) {
			exception.setMessage(AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getMessage());
			exception.setErrorCode(AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getCode());
			log.error(AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getCode() + " -> "
					+ AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getMessage() + ": " + e.getMessage());
			explist.add(exception);
			response.setErrors(explist);
			log.error(AppErrorMessages.PRN_GENERATION_FAILED.getCode() + " -> "
					+ AppErrorMessages.PRN_GENERATION_FAILED.getMessage() + ": " + e.getMessage());

		}

		return response;
	}

	private GeneratePRNResponseDTO getPrnURASOAP(GeneratePRNRequestDTO requestDTO, PrnTaxHeadEntity existingTaxHead) {
		log.info(AppConstants.STAGE_GENERATE_PRN + ":: getPrnURASOAP()");

		GeneratePRNResponseDTO responseDTO = new GeneratePRNResponseDTO();

		try {
			// Prepare the request DTO
			URASoapGeneratePRNRequestDTO uraRequestDTO = new URASoapGeneratePRNRequestDTO();
			uraRequestDTO.setUserName(mdaUsername);
			uraRequestDTO.setConcatenatedUsernamePasswordSignature(signedCredentials);
			uraRequestDTO.setEncryptedConcatenatedUsernamePassword(encryptedCredentials);
			uraRequestDTO.setReferenceNo("123"); // More clarification on this
			uraRequestDTO.setTaxHead(existingTaxHead.getTaxHeadCode());

			uraRequestDTO.setTaxPayerName(requestDTO.getFullName());
			// uraRequestDTO.setTaxPayerNIN(requestDTO.getNin()); // More clarification on
			// this
			uraRequestDTO.setAmount(Double.parseDouble(existingTaxHead.getTaxHeadAmount()));

			if (requestDTO.getNin() != null && !requestDTO.getNin().trim().isEmpty()) {
				uraRequestDTO.setTaxPayerNIN(requestDTO.getNin());
			} else {
				log.warn(AppConstants.STAGE_GENERATE_PRN + ":: TaxPayerNIN is missing; proceeding without it.");
			}

			// Call the SOAP service
			String response = soapServiceUtil.getPRN(uraRequestDTO);

			if (response != null) {
				log.info(AppSuccessMessages.SOAP_RESPONSE_SUCCESS.getMessage() + " -> " + response);

				URAGetPRNResultDTO resultDTO = null;

				resultDTO = soapServiceUtil.parseGetPRNResponse(response);

				if (resultDTO != null) {

					String errorCode = resultDTO.getErrorCode() != null ? resultDTO.getErrorCode().trim() : "";

					switch (errorCode) {
					case "APP006": // SOAP Authentication Error
						log.error(AppErrorMessages.SOAP_AUTHENTICATION_ERROR.getCode() + " -> "
								+ AppErrorMessages.SOAP_AUTHENTICATION_ERROR.getMessage());
						responseDTO.setCode(AppErrorMessages.SOAP_AUTHENTICATION_ERROR.getCode());
						responseDTO.setMessage(AppErrorMessages.SOAP_AUTHENTICATION_ERROR.getMessage());
						break;

					case "E000": // Success
						responseDTO.setCode("200");
						responseDTO.setMessage(resultDTO.getSearchCode());
						responseDTO.setData(resultDTO);
						break;

					case "E002": // Mandatory Field Missing / Conditional Mandatory field Missing
						log.error(AppErrorMessages.URA_MANDATORY_FIELD_MISSING.getCode() + " -> "
								+ AppErrorMessages.URA_MANDATORY_FIELD_MISSING.getMessage());
						responseDTO.setCode(AppErrorMessages.URA_MANDATORY_FIELD_MISSING.getCode());
						responseDTO.setMessage(AppErrorMessages.URA_MANDATORY_FIELD_MISSING.getMessage());
						break;

					case "E007": // Amount cannot be a negative value
						log.error(AppErrorMessages.URA_NEGATIVE_AMOUNT.getCode() + " -> "
								+ AppErrorMessages.URA_NEGATIVE_AMOUNT.getMessage());
						responseDTO.setCode(AppErrorMessages.URA_NEGATIVE_AMOUNT.getCode());
						responseDTO.setMessage(AppErrorMessages.URA_NEGATIVE_AMOUNT.getMessage());
						break;

					case "E008": // Expiry days cannot be a negative value
						log.error(AppErrorMessages.URA_NEGATIVE_EXPIRY_DAYS.getCode() + " -> "
								+ AppErrorMessages.URA_NEGATIVE_EXPIRY_DAYS.getMessage());
						responseDTO.setCode(AppErrorMessages.URA_NEGATIVE_EXPIRY_DAYS.getCode());
						responseDTO.setMessage(AppErrorMessages.URA_NEGATIVE_EXPIRY_DAYS.getMessage());
						break;

					case "E001": // Data Type Error
						log.error(AppErrorMessages.URA_DATA_TYPE_ERROR.getCode() + " -> "
								+ AppErrorMessages.URA_DATA_TYPE_ERROR.getMessage());
						responseDTO.setCode(AppErrorMessages.URA_DATA_TYPE_ERROR.getCode());
						responseDTO.setMessage(AppErrorMessages.URA_DATA_TYPE_ERROR.getMessage());
						break;

					case "E005": // Invalid Payment Mode
						log.error(AppErrorMessages.URA_INVALID_PAYMENT_MODE.getCode() + " -> "
								+ AppErrorMessages.URA_INVALID_PAYMENT_MODE.getMessage());
						responseDTO.setCode(AppErrorMessages.URA_INVALID_PAYMENT_MODE.getCode());
						responseDTO.setMessage(AppErrorMessages.URA_INVALID_PAYMENT_MODE.getMessage());
						break;

					case "E003": // Invalid Tax Head
						log.error(AppErrorMessages.URA_INVALID_TAX_HEAD.getCode() + " -> "
								+ AppErrorMessages.URA_INVALID_TAX_HEAD.getMessage());
						responseDTO.setCode(AppErrorMessages.URA_INVALID_TAX_HEAD.getCode());
						responseDTO.setMessage(AppErrorMessages.URA_INVALID_TAX_HEAD.getMessage());
						break;

					case "E006": // Invalid Bank Code
						log.error(AppErrorMessages.URA_INVALID_BANK_CODE.getCode() + " -> "
								+ AppErrorMessages.URA_INVALID_BANK_CODE.getMessage());
						responseDTO.setCode(AppErrorMessages.URA_INVALID_BANK_CODE.getCode());
						responseDTO.setMessage(AppErrorMessages.URA_INVALID_BANK_CODE.getMessage());
						break;

					default:
						// Handle unexpected error codes
						log.error(AppErrorMessages.URA_UNEXPECTED_ERROR.getCode() + " -> "
								+ AppErrorMessages.URA_UNEXPECTED_ERROR.getMessage() + ": " + errorCode);
						responseDTO.setCode(resultDTO.getErrorCode());
						responseDTO.setMessage(resultDTO.getErrorDesc());

						break;
					}

				} else {
					// Handle the case where resultDTO is null
					log.error(AppErrorMessages.SOAP_RESPONSE_CONVERSION_JAVA.getCode() + " -> "
							+ AppErrorMessages.SOAP_RESPONSE_CONVERSION_JAVA.getMessage());
					responseDTO.setCode(AppErrorMessages.SOAP_RESPONSE_CONVERSION_JAVA.getCode());
					responseDTO.setMessage(AppErrorMessages.SOAP_RESPONSE_CONVERSION_JAVA.getMessage());
				}

				log.info("Final GeneratePRNResponseDTO: Code -> {}, Message -> {}, Data -> {}", responseDTO.getCode(),
						responseDTO.getMessage(), responseDTO.getData());

				return responseDTO;

			} else {
				log.error(AppErrorMessages.SOAP_RESPONSE_NULL.getCode() + " -> "
						+ AppErrorMessages.SOAP_RESPONSE_NULL.getMessage());
				responseDTO.setCode(AppErrorMessages.SOAP_RESPONSE_NULL.getCode());
				responseDTO.setMessage(AppErrorMessages.SOAP_RESPONSE_NULL.getMessage());
				return responseDTO;
			}

		} catch (Exception e) {
			log.error(AppErrorMessages.PRN_GENERATION_FAILED.getCode() + " -> "
					+ AppErrorMessages.PRN_GENERATION_FAILED.getMessage() + ": " + e.getMessage());
			responseDTO.setCode(AppErrorMessages.PRN_GENERATION_FAILED.getCode());
			responseDTO.setMessage(AppErrorMessages.PRN_GENERATION_FAILED.getMessage());

			return responseDTO;
		}
	}

	/**
	 * This method consumes (saves) a PRN to database with the registrationId
	 * 
	 * @param requestDTO
	 * @return ConsumePrnResponseDTO
	 */
	public MainMosipResponseDTO<ConsumePRNResponseDTO> consumePrnAsUsed(ConsumePRNRequestDTO requestDTO) {

		log.info(AppConstants.STAGE_CONSUME_PRN_AS_USED + ":: consumePrnAsUsed()");

		MainMosipResponseDTO<ConsumePRNResponseDTO> response = new MainMosipResponseDTO<ConsumePRNResponseDTO>();
		ConsumePRNResponseDTO consumePrnResponseDTO = new ConsumePRNResponseDTO();

		response.setVersion(String.valueOf(version));
		response.setResponsetime(DateTimeFormatter.ofPattern(mosipDateTimeFormat).format(LocalDateTime.now()));

		List<ExceptionJSONInfoDTO> explist = new ArrayList<ExceptionJSONInfoDTO>();
		ExceptionJSONInfoDTO exception = new ExceptionJSONInfoDTO();

		try {

			if (!Objects.isNull(requestDTO) && requestDTO.getPrn() != null && requestDTO.getRegId() != null
					&& !requestDTO.getPrn().isEmpty() && !requestDTO.getRegId().isEmpty()) {

				consumePrnResponseDTO.setPrnNum(requestDTO.getPrn());

				PrnTransactionEntity existingPrnTransactionEntity = prnTransactionLogRepository
						.findByPrn(requestDTO.getPrn());

				if (!Objects.isNull(existingPrnTransactionEntity)) {
					consumePrnResponseDTO.setRegIdTaggedToPrn(existingPrnTransactionEntity.getRegId());
					consumePrnResponseDTO.setConsumedSucess(false);
					response.setResponse(consumePrnResponseDTO);

					exception.setMessage("PRN was already consumed");
					explist.add(exception);
					response.setErrors(explist);
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
				exception.setMessage("Bad request. Request {} missing.");
				explist.add(exception);
				response.setErrors(explist);
				log.error(AppErrorMessages.NPG_REQUEST_MISSING.getCode() + " -> "
						+ AppErrorMessages.NPG_REQUEST_MISSING.getMessage());
			}

		} catch (Exception e) {
			exception.setMessage(AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getMessage());
			exception.setErrorCode(AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getCode());
			log.error(AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getCode() + " -> "
					+ AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getMessage() + ": " + e.getMessage());
			explist.add(exception);
			response.setErrors(explist);
			log.error(AppErrorMessages.PRN_CONSUMPTION_FAILED.getCode() + " -> "
					+ AppErrorMessages.PRN_CONSUMPTION_FAILED.getMessage() + ": " + e.getMessage());
		}

		return response;
	}

	/**
	 * This method checks the transcation logs table if the PRN and registrationId
	 * are present
	 * 
	 * @param isPrnRegInLogsRequestDTO
	 * @return ispresent status
	 */
	public MainMosipResponseDTO<IsPRNRegInLogsResponseDTO> checkIfPrnAndRegIdPresentInLogs(
			IsPRNRegInLogsRequestDTO isPrnRegInLogsRequestDTO) {

		log.info(AppConstants.STAGE_CONSUME_PRN_AS_USED + ":: checkIfPrnAndRegIdPresentInLogs()");

		MainMosipResponseDTO<IsPRNRegInLogsResponseDTO> response = new MainMosipResponseDTO<IsPRNRegInLogsResponseDTO>();
		IsPRNRegInLogsResponseDTO isPrnRegInLogsResponseDTO = new IsPRNRegInLogsResponseDTO();

		response.setVersion(String.valueOf(version));
		response.setResponsetime(DateTimeFormatter.ofPattern(mosipDateTimeFormat).format(LocalDateTime.now()));

		List<ExceptionJSONInfoDTO> explist = new ArrayList<ExceptionJSONInfoDTO>();
		ExceptionJSONInfoDTO exception = new ExceptionJSONInfoDTO();

		try {

			if (!Objects.isNull(isPrnRegInLogsRequestDTO) && isPrnRegInLogsRequestDTO.getPrn() != null
					&& !isPrnRegInLogsRequestDTO.getPrn().trim().isEmpty()) {
				PrnTransactionEntity prnTransactionLogEntity = prnTransactionLogRepository
						.findByPrn(isPrnRegInLogsRequestDTO.getPrn());

				if (!Objects.isNull(prnTransactionLogEntity)) {

					isPrnRegInLogsResponseDTO.setPrn(isPrnRegInLogsRequestDTO.getPrn());
					isPrnRegInLogsResponseDTO.setRegIdTagged(prnTransactionLogEntity.getRegId());
					isPrnRegInLogsResponseDTO.setPresentInLogs(true);
					exception.setErrorCode(AppErrorMessages.PRN_ALREADY_CONSUMED_BY_DIFFERENT_REGID.getCode());
					exception.setMessage("PRN already consumed by Reg Id: " + prnTransactionLogEntity.getRegId());
					explist.add(exception);
					response.setErrors(explist);
					log.info(AppErrorMessages.PRN_ALREADY_CONSUMED_BY_DIFFERENT_REGID.getCode() + " -> "
							+ AppErrorMessages.PRN_ALREADY_CONSUMED_BY_DIFFERENT_REGID.getMessage());
				} else {
					isPrnRegInLogsResponseDTO.setPrn(isPrnRegInLogsRequestDTO.getPrn());
					isPrnRegInLogsResponseDTO.setPresentInLogs(false);
				}
				response.setResponse(isPrnRegInLogsResponseDTO);
			} else {
				exception.setMessage("Bad request. Request {} missing.");
				exception.setErrorCode(AppErrorMessages.NPG_REQUEST_MISSING.getCode());
				explist.add(exception);
				response.setErrors(explist);
				log.error(AppErrorMessages.NPG_REQUEST_MISSING.getCode() + " -> "
						+ AppErrorMessages.NPG_REQUEST_MISSING.getMessage());
			}
		} catch (Exception e) {
			exception.setMessage(AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getMessage());
			exception.setErrorCode(AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getCode());
			log.error(AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getCode() + " -> "
					+ AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getMessage() + ": " + e.getMessage());
			explist.add(exception);
			response.setErrors(explist);
		}
		return response;
	}

	/**
	 * This method returns a list of all consumed PRNs
	 * 
	 * @return list of all prns
	 */
	public MainMosipResponseDTO<PrnsConsumedListMetaDTO> findAllConsumedPrns() {

		log.info(AppConstants.STAGE_CONSUME_PRN_AS_USED + ":: findAllConsumedPrns()");
		MainMosipResponseDTO<PrnsConsumedListMetaDTO> response = new MainMosipResponseDTO<PrnsConsumedListMetaDTO>();
		PrnsConsumedListMetaDTO prnsListMetaDTO = new PrnsConsumedListMetaDTO();

		objectMapper = new ObjectMapper();

		response.setVersion(String.valueOf(version));
		response.setResponsetime(DateTimeFormatter.ofPattern(mosipDateTimeFormat).format(LocalDateTime.now()));

		List<ExceptionJSONInfoDTO> explist = new ArrayList<ExceptionJSONInfoDTO>();
		ExceptionJSONInfoDTO exception = new ExceptionJSONInfoDTO();

		try {

			List<PrnConsumedEntity> listPrns = prnConsumedRepository.findAll();

			if (!Objects.isNull(listPrns)) {

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
				exception.setMessage("No PRNs found");
				explist.add(exception);
				response.setErrors(explist);
				log.info(AppErrorMessages.NPG_ENTITIES_NOT_FOUND.getCode() + " -> "
						+ AppErrorMessages.NPG_ENTITIES_NOT_FOUND.getMessage() + ": PRN");

			}
		} catch (Exception ex) {
			exception.setMessage(AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getMessage());
			exception.setErrorCode(AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getCode());
			log.error(AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getCode() + " -> "
					+ AppErrorMessages.NPG_UNKNOWN_EXCEPTION.getMessage() + ": " + ex.getMessage());
			explist.add(exception);
			response.setErrors(explist);

		}
		return response;
	}
}
