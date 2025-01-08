package org.sc.themis.shared.assertion;

import org.sc.themis.shared.exception.ThemisException;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public class Assertions {

    private Assertions() {}

    public static <E extends ThemisException> void notNull(Object objToCheck, E rejected) throws E {
        if ( Objects.isNull( objToCheck ) ) {
            throw rejected;
        }
    }

    public static <E extends ThemisException> void notEmpty( Collection<?> objToCheck, E rejected) throws E {
        if ( objToCheck.isEmpty() ) {
            throw rejected;
        }
    }


    public static <O, E extends ThemisException> void isValid(O objToCheck, Predicate<O> predicate, E rejected) throws E {
        if ( !predicate.test( objToCheck ) ) {
            throw rejected;
        }
    }

    public static <E extends ThemisException> void isTrue(BooleanSupplier predicate, E rejected) throws E {
        if ( !predicate.getAsBoolean() ) {
            throw rejected;
        }
    }

    public static <E extends ThemisException> void isFalse(BooleanSupplier predicate, E rejected) throws E {
        if ( predicate.getAsBoolean() ) {
            throw rejected;
        }
    }


}
