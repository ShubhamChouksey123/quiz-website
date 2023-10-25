package com.shubham.app.service.questioncrud.exception;

public class InternalServerException extends Exception {

    private static final long serialVersionUID = 1L;
    private static final String ERROR_MESSAGE_DEFAULT = "Unable to complete request, please try again later";

    public InternalServerException(String message) {
        super(message);
    }

    public InternalServerException() {
        super(ERROR_MESSAGE_DEFAULT);
    }
}
