package com.kadioglumf.specification.search.definition;

import com.kadioglumf.specification.search.error.SearchErrorCode;
import com.thy.bagstar.bagstarcore.error.exception.BusinessException;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class SearchFieldRegistry {
  private final Map<String, SearchFieldDefinition> fields;
  private final SearchOptions options;
  private final List<SearchFetchDefinition> fetches;

  private SearchFieldRegistry(
      Collection<? extends SearchFieldDefinition> definitions,
      SearchOptions options,
      Collection<SearchFetchDefinition> fetches) {
    this.options = options == null ? SearchOptions.defaults() : options;
    this.fetches = fetches == null ? List.of() : List.copyOf(fetches);
    this.fields = new LinkedHashMap<>();
    for (SearchFieldDefinition definition : definitions) {
      this.fields.put(definition.apiFieldName(), definition);
    }
  }

  public static SearchFieldRegistry of(
      SearchOptions options, SearchFieldDefinition... definitions) {
    return new SearchFieldRegistry(Arrays.asList(definitions), options, List.of());
  }

  public static SearchFieldRegistry of(
      SearchOptions options,
      Collection<SearchFetchDefinition> fetches,
      SearchFieldDefinition... definitions) {
    return new SearchFieldRegistry(Arrays.asList(definitions), options, fetches);
  }

  public SearchOptions options() {
    return options;
  }

  public List<SearchFetchDefinition> fetches() {
    return fetches;
  }

  public Optional<SearchFieldDefinition> find(String apiFieldName) {
    return Optional.ofNullable(fields.get(apiFieldName));
  }

  public SearchFieldDefinition requireFilterable(String apiFieldName) {
    SearchFieldDefinition definition = requireKnown(apiFieldName);
    if (!definition.filterable()) {
      throw new BusinessException(SearchErrorCode.FIELD_NOT_FILTERABLE);
    }
    return definition;
  }

  public SearchFieldDefinition requireSortable(String apiFieldName) {
    SearchFieldDefinition definition = requireKnown(apiFieldName);
    if (!definition.sortable()) {
      throw new BusinessException(SearchErrorCode.FIELD_NOT_SORTABLE);
    }
    return definition;
  }

  public SearchFieldDefinition requireKnown(String apiFieldName) {
    SearchFieldDefinition definition = fields.get(apiFieldName);
    if (definition == null) {
      throw new BusinessException(SearchErrorCode.UNKNOWN_FIELD);
    }
    return definition;
  }
}
