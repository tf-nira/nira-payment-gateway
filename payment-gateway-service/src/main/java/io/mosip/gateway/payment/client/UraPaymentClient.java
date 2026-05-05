package io.mosip.gateway.payment.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import io.mosip.gateway.payment.constants.AppConstants;
import io.mosip.gateway.payment.constants.AppErrorMessages;
import io.mosip.gateway.payment.constants.AppSuccessMessages;
import io.mosip.gateway.payment.dto.request.CheckPRNStatusRequestDTO;
import io.mosip.gateway.payment.dto.request.GeneratePRNRequestDTO;
import io.mosip.gateway.payment.dto.response.CheckPRNStatusResponseDTO;
import io.mosip.gateway.payment.dto.response.GeneratePRNResponseDTO;
import io.mosip.gateway.payment.dto.ura.URACheckPRNStatusResultDTO;
import io.mosip.gateway.payment.dto.ura.URAGetPRNResultDTO;
import io.mosip.gateway.payment.dto.ura.URANTRAssessmentDTO;
import io.mosip.gateway.payment.dto.ura.URASoapCheckPRNStatusRequestDTO;
import io.mosip.gateway.payment.dto.ura.URASoapGeneratePRNRequestDTO;
import io.mosip.gateway.payment.entity.PrnTaxHeadEntity;
import io.mosip.gateway.payment.util.URASoapServiceUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UraPaymentClient {

    @Autowired
    private URASoapServiceUtil soapServiceUtil;

    @Value("${ura.user}")
    private String mdaUsername;

    @Value("${ura.signed}")
    private String signedCredentials;

    @Value("${ura.encrypted}")
    private String encryptedCredentials;
    
    public static final String SUCCESS_CODE = "SUC00";

    /**
     * Check PRN Status from URA
     */
    public CheckPRNStatusResponseDTO checkPrnStatus(CheckPRNStatusRequestDTO requestDTO) {

        log.info(AppConstants.STAGE_CHECK_PRN_STATUS + ":: UraPaymentClient.checkPrnStatus()");

        CheckPRNStatusResponseDTO responseDTO = new CheckPRNStatusResponseDTO();

        try {

            URASoapCheckPRNStatusRequestDTO uraRequestDTO = new URASoapCheckPRNStatusRequestDTO();
            uraRequestDTO.setStrPRN(requestDTO.getPrn());
            uraRequestDTO.setUserName(mdaUsername);
            uraRequestDTO.setConcatenatedUsernamePasswordSignature(signedCredentials);
            uraRequestDTO.setEncryptedConcatenatedUsernamePassword(encryptedCredentials);

            String response = soapServiceUtil.checkPRNStatus(uraRequestDTO);

            if (response == null) {

                responseDTO.setCode(AppErrorMessages.SOAP_RESPONSE_NULL.getCode());
                responseDTO.setMessage(AppErrorMessages.SOAP_RESPONSE_NULL.getMessage());
                return responseDTO;
            }

            log.info(AppSuccessMessages.SOAP_RESPONSE_SUCCESS.getMessage());

            URACheckPRNStatusResultDTO resultDTO =
                    soapServiceUtil.parseCheckPRNStatusResponse(response);

            if ("ERR01".equalsIgnoreCase(resultDTO.getErrorCode())) {

                responseDTO.setCode(AppErrorMessages.SOAP_AUTHENTICATION_ERROR.getCode());
                responseDTO.setMessage(AppErrorMessages.SOAP_AUTHENTICATION_ERROR.getMessage());
                return responseDTO;
            }

            String errorCode = resultDTO.getErrorCode();

            switch (errorCode) {

                case "A":
                case "D":
                case "R":
                case "T":
                    responseDTO.setCode("200");
                    responseDTO.setMessage(resultDTO.getStatusDesc());
                    responseDTO.setData(resultDTO);
                    break;

                case "N":
                    responseDTO.setCode(AppErrorMessages.URA_PRN_INVALID.getCode());
                    responseDTO.setMessage(AppErrorMessages.URA_PRN_INVALID.getMessage());
                    break;

                case "C":
                case "X":
                    responseDTO.setCode(AppErrorMessages.URA_PRN_CANCELLED.getCode());
                    responseDTO.setMessage(AppErrorMessages.URA_PRN_CANCELLED.getMessage());
                    break;

                case "DB001":
                case "PMT001":
                    responseDTO.setCode(AppErrorMessages.URA_PRN_NOT_FOUND.getCode());
                    responseDTO.setMessage(AppErrorMessages.URA_PRN_NOT_FOUND.getMessage());
                    break;

                default:
                    responseDTO.setCode(resultDTO.getErrorCode());
                    responseDTO.setMessage(resultDTO.getErrorDesc());
                    break;
            }

            return responseDTO;

        } catch (ResourceAccessException e) {

            log.error("URA service unreachable", e);

            responseDTO.setCode(AppErrorMessages.URA_SERVICE_UNREACHABLE.getCode());
            responseDTO.setMessage(AppErrorMessages.URA_SERVICE_UNREACHABLE.getMessage());
            return responseDTO;

        } catch (Exception e) {

            log.error(AppErrorMessages.PRN_CHECK_STATUS_FAILED.getMessage(), e);

            responseDTO.setCode(AppErrorMessages.PRN_CHECK_STATUS_FAILED.getCode());
            responseDTO.setMessage(AppErrorMessages.PRN_CHECK_STATUS_FAILED.getMessage());
            return responseDTO;
        }
    }

    /**
     * Generate PRN via URA
     */
    public GeneratePRNResponseDTO generatePrn(
            GeneratePRNRequestDTO requestDTO,
            PrnTaxHeadEntity existingTaxHead,
            Integer amountToSend) {

        log.info(AppConstants.STAGE_GENERATE_PRN + ":: UraPaymentClient.generatePrn()");

        GeneratePRNResponseDTO responseDTO = new GeneratePRNResponseDTO();

        try {

            URASoapGeneratePRNRequestDTO uraRequestDTO = new URASoapGeneratePRNRequestDTO();

            uraRequestDTO.setUserName(mdaUsername);
            uraRequestDTO.setConcatenatedUsernamePasswordSignature(signedCredentials);
            uraRequestDTO.setEncryptedConcatenatedUsernamePassword(encryptedCredentials);
            uraRequestDTO.setTaxHead(existingTaxHead.getTaxHeadCode());
            uraRequestDTO.setTaxPayerName(requestDTO.getFullName());

            Integer finalAmount = (amountToSend != null && amountToSend > 0)
                    ? amountToSend
                    : Integer.parseInt(existingTaxHead.getTaxHeadAmount());

            uraRequestDTO.setAmount(finalAmount);

            if (requestDTO.getNin() != null && !requestDTO.getNin().trim().isEmpty()) {
                uraRequestDTO.setTaxPayerNIN(requestDTO.getNin());
            }

            String currency = existingTaxHead.getCurrency();
            String response;

            Integer finalAmountToUse = finalAmount;

            // Assessment
            if (!"UGX".equalsIgnoreCase(currency)) {

                log.info("Foreign currency detected → converting to UGX using assessment API");

                String assessmentResponse = soapServiceUtil.getAssessmentAmount(uraRequestDTO);

                if (assessmentResponse == null) {
                    responseDTO.setCode(AppErrorMessages.SOAP_RESPONSE_NULL.getCode());
                    responseDTO.setMessage("Assessment response is null");
                    return responseDTO;
                }

                log.info(AppSuccessMessages.SOAP_RESPONSE_SUCCESS.getMessage() + " -> " + assessmentResponse);

                URANTRAssessmentDTO assessment =
                        soapServiceUtil.parseGetAssessmentResponse(assessmentResponse);

                if (assessment == null) {
                	responseDTO.setCode(AppErrorMessages.SOAP_RESPONSE_NULL.getCode());
                    responseDTO.setMessage(AppErrorMessages.SOAP_RESPONSE_NULL.getMessage());
                    return responseDTO;
                }

                // Successful status check
                if (!"SUC00".equalsIgnoreCase(assessment.getStatusCode())) {

                    log.error("Amount Assessment (Currency Conversion) failed: {} - {}",
                            assessment.getStatusCode(),
                            assessment.getStatusDesc());

                    responseDTO.setCode(assessment.getStatusCode());
                    responseDTO.setMessage("Amount Assessment (Currency Conversion) failed: " + assessment.getStatusDesc());
                    return responseDTO;
                }

                finalAmountToUse = assessment.getNtrAmount();

                log.info("Converted UGX Amount: {}", finalAmountToUse);

                uraRequestDTO.setAmount(finalAmountToUse);
            }

            // PRN Generation
            log.info("Using normal SOAP method (GetPRN)");

            response = soapServiceUtil.getPRN(uraRequestDTO);

            if (response == null) {
                responseDTO.setCode(AppErrorMessages.SOAP_RESPONSE_NULL.getCode());
                responseDTO.setMessage(AppErrorMessages.SOAP_RESPONSE_NULL.getMessage());
                return responseDTO;
            }

            log.info(AppSuccessMessages.SOAP_RESPONSE_SUCCESS.getMessage() + " -> " + response);

            URAGetPRNResultDTO resultDTO =
                    soapServiceUtil.parseGetPRNResponse(response);

            String errorCode = resultDTO.getErrorCode() != null
                    ? resultDTO.getErrorCode().trim()
                    : "";

            String errorDesc = resultDTO.getErrorDesc();
            String searchCode = resultDTO.getSearchCode();

            responseDTO.setData(resultDTO);

            switch (errorCode) {

                case "APP006":
                    responseDTO.setCode(AppErrorMessages.SOAP_AUTHENTICATION_ERROR.getCode());
                    responseDTO.setMessage(AppErrorMessages.SOAP_AUTHENTICATION_ERROR.getMessage());
                    break;

                case "E000":
                    responseDTO.setCode("200");
                    responseDTO.setMessage(searchCode);
                    responseDTO.setAmountUsed(finalAmountToUse.toString());
                    break;

                case "E002":
                    responseDTO.setCode(AppErrorMessages.URA_MANDATORY_FIELD_MISSING.getCode());
                    responseDTO.setMessage(AppErrorMessages.URA_MANDATORY_FIELD_MISSING.getMessage());
                    break;

                case "E007":
                    responseDTO.setCode(AppErrorMessages.URA_NEGATIVE_AMOUNT.getCode());
                    responseDTO.setMessage(AppErrorMessages.URA_NEGATIVE_AMOUNT.getMessage());
                    break;

                case "E008":
                    responseDTO.setCode(AppErrorMessages.URA_NEGATIVE_EXPIRY_DAYS.getCode());
                    responseDTO.setMessage(AppErrorMessages.URA_NEGATIVE_EXPIRY_DAYS.getMessage());
                    break;

                case "E001":
                    responseDTO.setCode(AppErrorMessages.URA_DATA_TYPE_ERROR.getCode());
                    responseDTO.setMessage(AppErrorMessages.URA_DATA_TYPE_ERROR.getMessage());
                    break;

                case "E005":
                    responseDTO.setCode(AppErrorMessages.URA_INVALID_PAYMENT_MODE.getCode());
                    responseDTO.setMessage(AppErrorMessages.URA_INVALID_PAYMENT_MODE.getMessage());
                    break;

                case "E003":
                    responseDTO.setCode(AppErrorMessages.URA_INVALID_TAX_HEAD.getCode());
                    responseDTO.setMessage(AppErrorMessages.URA_INVALID_TAX_HEAD.getMessage());
                    break;

                case "E006":
                    responseDTO.setCode(AppErrorMessages.URA_INVALID_BANK_CODE.getCode());
                    responseDTO.setMessage(AppErrorMessages.URA_INVALID_BANK_CODE.getMessage());
                    break;

                default:
                    responseDTO.setCode(errorCode);
                    responseDTO.setMessage(errorDesc);
                    break;
            }

            return responseDTO;

        } catch (ResourceAccessException e) {

            log.error("URA service unreachable", e);

            responseDTO.setCode(AppErrorMessages.URA_SERVICE_UNREACHABLE.getCode());
            responseDTO.setMessage(AppErrorMessages.URA_SERVICE_UNREACHABLE.getMessage());
            return responseDTO;

        } catch (Exception e) {

            log.error(AppErrorMessages.PRN_GENERATION_FAILED.getMessage(), e);

            responseDTO.setCode(AppErrorMessages.PRN_GENERATION_FAILED.getCode());
            responseDTO.setMessage(AppErrorMessages.PRN_GENERATION_FAILED.getMessage());
            return responseDTO;
        }
    }
}