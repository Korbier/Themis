package org.sc.themis.engine.exception;

import org.sc.themis.shared.exception.ThemisException;

public class EngineGamestateNotFoundException extends ThemisException {

    private final static String MESSAGE = "No gamestate found. Use Engine::setGamestate to set one.";

    public EngineGamestateNotFoundException() {
        super(MESSAGE);
    }
}
