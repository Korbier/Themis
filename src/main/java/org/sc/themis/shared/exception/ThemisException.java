package org.sc.themis.shared.exception;

public class ThemisException extends Exception {

    public ThemisException() {
    }

    public ThemisException(String message) {
        super(message);
    }

    public ThemisException(String message, Throwable cause) {
        super(message, cause);
    }

}
