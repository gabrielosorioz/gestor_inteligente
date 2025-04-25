package com.gabrielosorio.gestor_inteligente.exception;

public class SaleProcessingException extends Exception {
    public SaleProcessingException(String message) {
        super(message);
    }

    public SaleProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

