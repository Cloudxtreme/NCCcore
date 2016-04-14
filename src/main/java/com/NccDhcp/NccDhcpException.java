package com.NccDhcp;

public class NccDhcpException extends Exception {
    private String message;

    public NccDhcpException(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
