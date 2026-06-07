package com.superclinic.doctorassistant.common.exception;

public class DoctorAssistantException extends RuntimeException {

    public DoctorAssistantException(String message) {
        super(message);
    }

    public DoctorAssistantException(String message, Throwable cause) {
        super(message, cause);
    }
}
