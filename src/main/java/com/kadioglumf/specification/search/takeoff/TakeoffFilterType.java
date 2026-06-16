package com.kadioglumf.specification.search.takeoff;

import com.kadioglumf.specification.search.definition.SearchOperator;
import com.kadioglumf.specification.search.definition.SearchOptions;
import com.kadioglumf.specification.search.error.SearchErrorCode;
import com.thy.bagstar.bagstarcore.error.exception.BusinessException;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

public enum TakeoffFilterType {
  TEXT,
  CHECKBOX,
  RADIO,
  DATEPICKER,
  TREEVIEW;

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

  SearchOperator defaultOperator(Object value, SearchOptions options) {
    return switch (this) {
      case TEXT -> options.defaultTextOperator();
      case CHECKBOX, TREEVIEW -> SearchOperator.IN;
      case RADIO -> SearchOperator.EQUAL;
      case DATEPICKER -> isRangeValue(value) ? SearchOperator.BETWEEN : SearchOperator.EQUAL;
    };
  }

  private static boolean isRangeValue(Object value) {
    if (value instanceof Collection<?> collection) {
      return collection.size() == 2;
    }
    if (value instanceof Object[] array) {
      return array.length == 2;
    }
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
