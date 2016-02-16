package com.NccAccounts;

public class NccAccountsException extends Exception {
    private String message;

    public NccAccountsException(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

}
