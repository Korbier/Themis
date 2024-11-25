package org.sc.themis.window.exception;

import org.sc.themis.shared.exception.ThemisException;

public class WindowVukanNotSupportedException extends ThemisException {

    private final static String MESSAGE = "Cannot find a compatible Vulkan installable client driver (ICD)";

    public WindowVukanNotSupportedException() {
        super(MESSAGE);
    }
}
