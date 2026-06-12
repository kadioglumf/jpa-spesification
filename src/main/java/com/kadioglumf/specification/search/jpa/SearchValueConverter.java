package com.kadioglumf.specification.search.jpa;

import com.kadioglumf.specification.search.definition.SearchFieldDefinition;
import com.kadioglumf.specification.search.exception.SearchErrorCode;
import com.kadioglumf.specification.search.exception.SearchValidationException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SearchValueConverter {
  public Object convertSingle(SearchFieldDefinition definition, Object value) {
    if (value instanceof Collection<?> collection) {
      if (collection.size() != 1) {
        throw new SearchValidationException(
            SearchErrorCode.INVALID_VALUE_TYPE, "Single filter value is required.");
      }
      value = collection.iterator().next();
    }
    if (value == null) {
      throw new SearchValidationException(SearchErrorCode.EMPTY_VALUE, "Filter value is required.");
    }
    value = unwrapSingleDateSelection(value);
    return switch (definition.fieldType()) {
      case STRING -> value.toString();
      case LONG -> convertLong(value);
      case INTEGER -> convertInteger(value);
      case BIG_DECIMAL -> convertBigDecimal(value);
      case BOOLEAN -> convertBoolean(value);
      case LOCAL_DATE -> convertLocalDate(value);
      case LOCAL_DATE_TIME -> convertLocalDateTime(value);
      case OFFSET_DATE_TIME -> convertOffsetDateTime(value);
      case INSTANT -> convertInstant(value);
      case ENUM -> convertEnum(definition, value);
    };
  }

  public List<Object> convertList(SearchFieldDefinition definition, Object value) {
    List<Object> rawValues = toList(value);
    List<Object> convertedValues = new ArrayList<>(rawValues.size());
    for (Object rawValue : rawValues) {
      convertedValues.add(convertSingle(definition, rawValue));
    }
    return convertedValues;
  }

  public List<Object> convertRange(SearchFieldDefinition definition, Object value) {
    List<Object> rawValues = toRangeList(value);
    if (rawValues.size() != 2) {
      throw new SearchValidationException(
          SearchErrorCode.INVALID_VALUE_TYPE, "Range filter requires two values.");
    }
    return List.of(
        convertSingle(definition, rawValues.get(0)), convertSingle(definition, rawValues.get(1)));
  }

  private List<Object> toList(Object value) {
    if (value instanceof Collection<?> collection) {
      return new ArrayList<>(collection);
    }
    if (value instanceof Object[] array) {
      return List.of(array);
    }
    return List.of(value);
  }

  private List<Object> toRangeList(Object value) {
    if (value instanceof Map<?, ?> map) {
      Object from = firstPresent(map, "from", "start", "min", "begin", "startDate");
      Object to = firstPresent(map, "to", "end", "max", "finish", "endDate");
      List<Object> rangeValues = new ArrayList<>(2);
      rangeValues.add(from);
      rangeValues.add(to);
      return rangeValues;
    }
    return toList(value);
  }

  private Object firstPresent(Map<?, ?> map, String... keys) {
    for (String key : keys) {
      if (map.containsKey(key)) {
        return map.get(key);
      }
    }
    throw new SearchValidationException(
        SearchErrorCode.INVALID_VALUE_TYPE, "Range filter requires from and to values.");
  }

  private Object unwrapSingleDateSelection(Object value) {
    if (value instanceof Map<?, ?> map) {
      for (String key : List.of("date", "value", "selectedDate", "singleDate")) {
        if (map.containsKey(key)) {
          return map.get(key);
        }
      }
    }
    return value;
  }

  private Long convertLong(Object value) {
    if (value instanceof Number number) {
      return number.longValue();
    }
    try {
      return Long.valueOf(value.toString());
    } catch (NumberFormatException ex) {
      throw new SearchValidationException(SearchErrorCode.INVALID_NUMBER, "Invalid number value.");
    }
  }

  private Integer convertInteger(Object value) {
    if (value instanceof Number number) {
      return number.intValue();
    }
    try {
      return Integer.valueOf(value.toString());
    } catch (NumberFormatException ex) {
      throw new SearchValidationException(SearchErrorCode.INVALID_NUMBER, "Invalid number value.");
    }
  }

  private BigDecimal convertBigDecimal(Object value) {
    if (value instanceof BigDecimal bigDecimal) {
      return bigDecimal;
    }
    if (value instanceof Number number) {
      return BigDecimal.valueOf(number.doubleValue());
    }
    try {
      return new BigDecimal(value.toString());
    } catch (NumberFormatException ex) {
      throw new SearchValidationException(SearchErrorCode.INVALID_NUMBER, "Invalid number value.");
    }
  }

  private Boolean convertBoolean(Object value) {
    if (value instanceof Boolean booleanValue) {
      return booleanValue;
    }
    String normalized = value.toString().trim().toLowerCase(Locale.ROOT);
    if ("true".equals(normalized)) {
      return true;
    }
    if ("false".equals(normalized)) {
      return false;
    }
    throw new SearchValidationException(SearchErrorCode.INVALID_BOOLEAN, "Invalid boolean value.");
  }

  private LocalDate convertLocalDate(Object value) {
    if (value instanceof LocalDate localDate) {
      return localDate;
    }
    try {
      return LocalDate.parse(value.toString());
    } catch (RuntimeException ex) {
      throw new SearchValidationException(SearchErrorCode.INVALID_DATE, "Invalid date value.");
    }
  }

  private LocalDateTime convertLocalDateTime(Object value) {
    if (value instanceof LocalDateTime localDateTime) {
      return localDateTime;
    }
    try {
      return LocalDateTime.parse(value.toString());
    } catch (RuntimeException ex) {
      throw new SearchValidationException(SearchErrorCode.INVALID_DATE, "Invalid date-time value.");
    }
  }

  private OffsetDateTime convertOffsetDateTime(Object value) {
    if (value instanceof OffsetDateTime offsetDateTime) {
      return offsetDateTime;
    }
    try {
      return OffsetDateTime.parse(value.toString());
    } catch (RuntimeException ex) {
      throw new SearchValidationException(
          SearchErrorCode.INVALID_DATE, "Invalid offset date-time value.");
    }
  }

  private Instant convertInstant(Object value) {
    if (value instanceof Instant instant) {
      return instant;
    }
    try {
      return Instant.parse(value.toString());
    } catch (RuntimeException ex) {
      throw new SearchValidationException(SearchErrorCode.INVALID_DATE, "Invalid instant value.");
    }
  }

  private Enum<?> convertEnum(SearchFieldDefinition definition, Object value) {
    Class<? extends Enum<?>> enumClass =
        definition
            .enumClass()
            .orElseThrow(
                () ->
                    new SearchValidationException(
                        SearchErrorCode.INVALID_VALUE_TYPE, "Enum field is not configured."));
    String normalized = value.toString().trim();
    for (Enum<?> enumConstant : enumClass.getEnumConstants()) {
      if (enumConstant.name().equalsIgnoreCase(normalized)) {
        return enumConstant;
      }
    }
    throw new SearchValidationException(SearchErrorCode.INVALID_VALUE_TYPE, "Invalid enum value.");
  }
}
