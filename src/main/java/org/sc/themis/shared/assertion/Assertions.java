package org.sc.themis.shared.assertion;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import org.sc.themis.shared.exception.ThemisException;

/** Assertions used to check validity of method input and trigger contextual exception. */
public class Assertions {

  /** Private constructor. */
  private Assertions() {}

  /**
   * Not null assertion.
   *
   * @param objToCheck Object to check
   * @param rejected Exception rejected if assertion fails
   * @param <E> Exception type
   * @throws E Rejected exception
   */
  public static <E extends ThemisException> void notNull(Object objToCheck, E rejected) throws E {
    if (Objects.isNull(objToCheck)) {
      throw rejected;
    }
  }

  /**
   * Not empty assertion.
   *
   * @param objToCheck Object to check
   * @param rejected Exception rejected if assertion fails
   * @param <E> Exception type
   * @throws E Rejected exception
   */
  public static <E extends ThemisException> void notEmpty(Collection<?> objToCheck, E rejected)
      throws E {
    if (objToCheck.isEmpty()) {
      throw rejected;
    }
  }

  /**
   * is valid assertion.
   *
   * @param objToCheck Object to check
   * @param predicate predicate used to check validity
   * @param rejected Exception rejected if assertion fails
   * @param <E> Exception type
   * @throws E Rejected exception
   */
  public static <O, E extends ThemisException> void isValid(
      O objToCheck, Predicate<O> predicate, E rejected) throws E {
    if (!predicate.test(objToCheck)) {
      throw rejected;
    }
  }

  /**
   * is true assertion.
   *
   * @param predicate predicate used to check validity
   * @param rejected Exception rejected if assertion fails
   * @param <E> Exception type
   * @throws E Rejected exception
   */
  public static <E extends ThemisException> void isTrue(BooleanSupplier predicate, E rejected)
      throws E {
    if (!predicate.getAsBoolean()) {
      throw rejected;
    }
  }

  /**
   * is false assertion.
   *
   * @param predicate predicate used to check validity
   * @param rejected Exception rejected if assertion fails
   * @param <E> Exception type
   * @throws E Rejected exception
   */
  public static <E extends ThemisException> void isFalse(BooleanSupplier predicate, E rejected)
      throws E {
    if (predicate.getAsBoolean()) {
      throw rejected;
    }
  }
}
