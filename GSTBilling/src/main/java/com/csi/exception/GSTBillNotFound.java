package com.csi.exception;

public class GSTBillNotFound extends RuntimeException{
    public GSTBillNotFound(String message) {
        super(message);
    }
}