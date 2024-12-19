package org.sc.themis.scene.exception;

import org.sc.themis.shared.exception.ThemisException;

import java.nio.file.Path;

public class ModelFileNotFoundException extends ThemisException {

    private final static String MESSAGE = "Model file %s not found";

    public ModelFileNotFoundException( Path path ) {
        super( MESSAGE.formatted( path.toAbsolutePath().toString() ) );
    }

}

