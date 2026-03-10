package io.mosip.gateway.payment.util;

import java.util.ArrayList;
import java.util.List;

import io.mosip.gateway.payment.constants.AppErrorMessages;
import io.mosip.gateway.payment.dto.response.ExceptionJSONInfoDTO;
import io.mosip.gateway.payment.dto.response.MainMosipResponseDTO;

public class ResponseUtil {

    public static <T> void setError(
            MainMosipResponseDTO<T> response,
            String code,
            String message) {

        List<ExceptionJSONInfoDTO> errors = new ArrayList<>();

        ExceptionJSONInfoDTO exception = new ExceptionJSONInfoDTO();
        exception.setErrorCode(code);
        exception.setMessage(message);

        errors.add(exception);

        response.setErrors(errors);
    }

    public static <T> void setError(
            MainMosipResponseDTO<T> response,
            AppErrorMessages error) {

        setError(response, error.getCode(), error.getMessage());
    }
}