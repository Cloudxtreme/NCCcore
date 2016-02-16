package com.NccSystem.SQL;

public class NccQueryException extends Exception {
    private String message;

    public NccQueryException(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
