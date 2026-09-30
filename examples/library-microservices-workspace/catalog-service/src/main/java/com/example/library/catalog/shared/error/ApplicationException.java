package com.example.library.catalog.shared.error;

public class ApplicationException extends RuntimeException {

    private final int status;
    private final String code;
    private final String messageKey;

    public ApplicationException(int status, String code, String messageKey) {
        this.status = status;
        this.code = code;
        this.messageKey = messageKey;
    }

    public int status() {
        return status;
    }

    public String code() {
        return code;
    }

    public String messageKey() {
        return messageKey;
    }
}
