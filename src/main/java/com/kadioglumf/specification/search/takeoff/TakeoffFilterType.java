package com.kadioglumf.specification.search.takeoff;

import com.kadioglumf.specification.search.definition.SearchOperator;
import com.kadioglumf.specification.search.error.SearchErrorCode;
import com.thy.bagstar.bagstarcore.error.exception.BusinessException;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

public enum TakeoffFilterType {
  NUMBER,
  TEXT,
  BOOLEAN,
  DATE;

  public static TakeoffFilterType from(String value) {
    if (value == null || value.isBlank()) {
      return TEXT;
    }
    try {
      return TakeoffFilterType.valueOf(value.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      throw new BusinessException(SearchErrorCode.UNSUPPORTED_FILTER_TYPE);
    }
  }

  SearchOperator defaultOperator(Object value) {
    return switch (this) {
      case NUMBER -> isListValue(value) ? SearchOperator.IN : SearchOperator.EQUAL;
      case TEXT -> SearchOperator.CONTAINS;
      case BOOLEAN -> SearchOperator.EQUAL;
      case DATE ->
          isRangeMap(value)
              ? SearchOperator.BETWEEN
              : isListValue(value) ? SearchOperator.IN : SearchOperator.EQUAL;
    };
  }

  private static boolean isListValue(Object value) {
    if (value instanceof Collection<?>) {
      return true;
    }
    return value instanceof Object[];
  }

  private static boolean isRangeMap(Object value) {
    if (value instanceof Map<?, ?> map) {
      return hasAny(map, "from", "start", "min", "begin", "startDate")
          && hasAny(map, "to", "end", "max", "finish", "endDate");
    }
    return false;
  }

  private static boolean hasAny(Map<?, ?> map, String... keys) {
    for (String key : keys) {
      if (map.containsKey(key)) {
        return true;
      }
    }
    return false;
  }
}
