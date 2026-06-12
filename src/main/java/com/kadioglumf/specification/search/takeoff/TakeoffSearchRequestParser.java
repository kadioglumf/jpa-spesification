package com.kadioglumf.specification.search.takeoff;

import com.kadioglumf.specification.search.definition.SearchFieldDefinition;
import com.kadioglumf.specification.search.definition.SearchFieldRegistry;
import com.kadioglumf.specification.search.definition.SearchFieldType;
import com.kadioglumf.specification.search.definition.SearchOperator;
import com.kadioglumf.specification.search.definition.SearchOptions;
import com.kadioglumf.specification.search.exception.SearchErrorCode;
import com.kadioglumf.specification.search.exception.SearchValidationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TakeoffSearchRequestParser {
  public List<NormalizedSearchFilter> parseFilters(
      TakeoffTableRequest request, SearchFieldRegistry registry) {
    SearchOptions options = registry.options();
    if (request == null) {
      throw new SearchValidationException(
          SearchErrorCode.EMPTY_VALUE, "Search request is required.");
    }
    if (request.filters() == null) {
      throw new SearchValidationException(SearchErrorCode.EMPTY_VALUE, "Filters are required.");
    }
    if (request.filters().size() > options.maxFilters()) {
      throw new SearchValidationException(SearchErrorCode.TOO_MANY_FILTERS, "Too many filters.");
    }
    List<NormalizedSearchFilter> normalizedFilters = new ArrayList<>();
    for (TakeoffTableFilter filter : request.filters()) {
      if (filter == null || filter.field() == null || filter.field().isBlank()) {
        throw new SearchValidationException(SearchErrorCode.UNKNOWN_FIELD, "Unknown search field.");
      }
      SearchFieldDefinition definition = registry.requireFilterable(filter.field());
      TakeoffFilterType takeoffType = TakeoffFilterType.from(filter.type());
      SearchOperator operator =
          definition
              .defaultOperatorOverrides()
              .getOrDefault(takeoffType, takeoffType.defaultOperator(filter.value(), options));
      if (shouldIgnore(filter.value(), takeoffType, options)) {
        continue;
      }
      validateValuePresence(filter.value(), takeoffType);
      operator = normalizeBooleanCheckboxOperator(definition, operator, filter.value());
      validateOperator(definition, operator);
      validateInSize(operator, filter.value(), options);
      normalizedFilters.add(
          new NormalizedSearchFilter(definition, takeoffType, operator, filter.value()));
    }
    return normalizedFilters;
  }

  private boolean shouldIgnore(Object value, TakeoffFilterType type, SearchOptions options) {
    if (type == TakeoffFilterType.TEXT
        && options.ignoreBlankTextFilters()
        && value instanceof String stringValue
        && stringValue.isBlank()) {
      return true;
    }
    return type == TakeoffFilterType.CHECKBOX
        && options.ignoreEmptyCheckboxFilters()
        && collectionSize(value) == 0;
  }

  private void validateValuePresence(Object value, TakeoffFilterType type) {
    if (value == null) {
      throw new SearchValidationException(SearchErrorCode.EMPTY_VALUE, "Filter value is required.");
    }
    if (value instanceof String stringValue
        && stringValue.isBlank()
        && type != TakeoffFilterType.TEXT) {
      throw new SearchValidationException(SearchErrorCode.EMPTY_VALUE, "Filter value is required.");
    }
    if ((type == TakeoffFilterType.CHECKBOX || type == TakeoffFilterType.TREEVIEW)
        && collectionSize(value) == 0) {
      throw new SearchValidationException(SearchErrorCode.EMPTY_VALUE, "Filter value is required.");
    }
  }

  private SearchOperator normalizeBooleanCheckboxOperator(
      SearchFieldDefinition definition, SearchOperator operator, Object value) {
    if (definition.fieldType() == SearchFieldType.BOOLEAN
        && operator == SearchOperator.IN
        && collectionSize(value) == 1) {
      return SearchOperator.EQUAL;
    }
    return operator;
  }

  private void validateOperator(SearchFieldDefinition definition, SearchOperator operator) {
    if (!definition.allowedOperators().contains(operator)) {
      throw new SearchValidationException(
          SearchErrorCode.UNSUPPORTED_OPERATOR, "Unsupported operator for field.");
    }
  }

  private void validateInSize(SearchOperator operator, Object value, SearchOptions options) {
    if ((operator == SearchOperator.IN || operator == SearchOperator.NOT_IN)
        && collectionSize(value) > options.maxInValues()) {
      throw new SearchValidationException(
          SearchErrorCode.TOO_MANY_IN_VALUES, "Too many filter values.");
    }
  }

  private int collectionSize(Object value) {
    if (value == null) {
      return 0;
    }
    if (value instanceof Collection<?> collection) {
      return collection.size();
    }
    if (value instanceof Object[] array) {
      return array.length;
    }
    if (value instanceof Map<?, ?> map) {
      return map.size();
    }
    return 1;
  }
}
