package com.kadioglumf.specification.search.definition;

import com.kadioglumf.specification.search.exception.SearchErrorCode;
import com.kadioglumf.specification.search.exception.SearchValidationException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class SearchFieldRegistry {
  private final Map<String, SearchFieldDefinition> fields;
  private final SearchOptions options;

  private SearchFieldRegistry(
      Collection<? extends SearchFieldDefinition> definitions, SearchOptions options) {
    this.options = options == null ? SearchOptions.defaults() : options;
    this.fields = new LinkedHashMap<>();
    for (SearchFieldDefinition definition : definitions) {
      this.fields.put(definition.apiFieldName(), definition);
    }
  }

  public static SearchFieldRegistry of(
      SearchOptions options, SearchFieldDefinition... definitions) {
    return new SearchFieldRegistry(Arrays.asList(definitions), options);
  }

  public SearchOptions options() {
    return options;
  }

  public Optional<SearchFieldDefinition> find(String apiFieldName) {
    return Optional.ofNullable(fields.get(apiFieldName));
  }

  public SearchFieldDefinition requireFilterable(String apiFieldName) {
    SearchFieldDefinition definition = requireKnown(apiFieldName);
    if (!definition.filterable()) {
      throw new SearchValidationException(
          SearchErrorCode.FIELD_NOT_FILTERABLE, "Field is not filterable.");
    }
    return definition;
  }

  public SearchFieldDefinition requireSortable(String apiFieldName) {
    SearchFieldDefinition definition = requireKnown(apiFieldName);
    if (!definition.sortable()) {
      throw new SearchValidationException(
          SearchErrorCode.FIELD_NOT_SORTABLE, "Field is not sortable.");
    }
    return definition;
  }

  public SearchFieldDefinition requireKnown(String apiFieldName) {
    SearchFieldDefinition definition = fields.get(apiFieldName);
    if (definition == null) {
      throw new SearchValidationException(SearchErrorCode.UNKNOWN_FIELD, "Unknown search field.");
    }
    return definition;
  }
}
