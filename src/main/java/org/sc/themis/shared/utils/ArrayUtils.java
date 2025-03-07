package org.sc.themis.shared.utils;

import java.util.Arrays;

/** Array centered tools. */
public class ArrayUtils {

  private ArrayUtils() {}

  /**
   * Merge an object and arrays in one array.
   *
   * @param object Object to merge
   * @param others Arrays to merge
   * @param <O> Object type
   * @return An array contains all input data
   */
  public static <O> O[] merge(O object, O... others) {
    O[] copy = Arrays.copyOf(others, others.length + 1);
    copy[0] = object;
    System.arraycopy(others, 0, copy, 1, others.length);
    return copy;
  }
}
