package org.sc.themis.window.exception;

import org.sc.themis.shared.exception.ThemisException;

public class WindowVideoModeNotSupportedException extends ThemisException {

    private final static String MESSAGE = "Video mode not supported";

    public WindowVideoModeNotSupportedException() {
        super(MESSAGE);
    }
}
