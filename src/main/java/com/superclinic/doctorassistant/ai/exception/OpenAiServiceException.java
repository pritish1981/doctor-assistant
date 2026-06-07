package com.superclinic.doctorassistant.ai.exception;

import com.superclinic.doctorassistant.common.exception.DoctorAssistantException;
import org.springframework.http.HttpStatusCode;

public class OpenAiServiceException extends DoctorAssistantException {

    private final HttpStatusCode statusCode;

    public OpenAiServiceException(String message, HttpStatusCode statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public OpenAiServiceException(String message, HttpStatusCode statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }
}
