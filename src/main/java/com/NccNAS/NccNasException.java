package com.NccNAS;

public class NccNasException extends Exception {
    private String message;

    public NccNasException(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

}
