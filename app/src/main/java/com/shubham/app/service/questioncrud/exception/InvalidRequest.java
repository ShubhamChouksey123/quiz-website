package com.shubham.app.service.questioncrud.exception;

public class InvalidRequest extends Exception {

    private static final long serialVersionUID = 1L;
    private static final String ERROR_MESSAGE_DEFAULT = "Invalid parameters : ";

    public InvalidRequest(String message) {
        super(ERROR_MESSAGE_DEFAULT + message);
    }

    public InvalidRequest() {
        super(ERROR_MESSAGE_DEFAULT);
    }
}
