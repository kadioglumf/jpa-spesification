package com.kadioglumf.specification.search.definition;

import org.springframework.data.domain.Sort;

public record SearchOptions(
    int defaultPageSize,
    int maxPageSize,
    int maxFilters,
    int maxInValues,
    boolean ignoreBlankTextFilters,
    boolean ignoreEmptyCheckboxFilters,
    boolean caseInsensitiveTextSearch,
    SearchOperator defaultTextOperator,
    String defaultSortField,
    Sort.Direction defaultSortDirection) {
  public SearchOptions {
    if (defaultPageSize < 1) {
      defaultPageSize = 20;
    }
    if (maxPageSize < 1) {
      maxPageSize = 100;
    }
    if (maxFilters < 1) {
      maxFilters = 20;
    }
    if (maxInValues < 1) {
      maxInValues = 100;
    }
    if (defaultTextOperator == null) {
      defaultTextOperator = SearchOperator.CONTAINS;
    }
    if (defaultSortDirection == null) {
      defaultSortDirection = Sort.Direction.DESC;
    }
  }

  public static SearchOptions defaults() {
    return new SearchOptions(
        20, 100, 20, 100, true, true, true, SearchOperator.CONTAINS, "id", Sort.Direction.DESC);
  }

  public static Builder builder() {
    return new Builder();
  }

  public SearchOptions withDefaultSortField(String defaultSortField) {
    return new SearchOptions(
        defaultPageSize,
        maxPageSize,
        maxFilters,
        maxInValues,
        ignoreBlankTextFilters,
        ignoreEmptyCheckboxFilters,
        caseInsensitiveTextSearch,
        defaultTextOperator,
        defaultSortField,
        defaultSortDirection);
  }

  public SearchOptions withMaxPageSize(int maxPageSize) {
    return new SearchOptions(
        defaultPageSize,
        maxPageSize,
        maxFilters,
        maxInValues,
        ignoreBlankTextFilters,
        ignoreEmptyCheckboxFilters,
        caseInsensitiveTextSearch,
        defaultTextOperator,
        defaultSortField,
        defaultSortDirection);
  }

  public SearchOptions withDefaultSortDirection(Sort.Direction defaultSortDirection) {
    return new SearchOptions(
        defaultPageSize,
        maxPageSize,
        maxFilters,
        maxInValues,
        ignoreBlankTextFilters,
        ignoreEmptyCheckboxFilters,
        caseInsensitiveTextSearch,
        defaultTextOperator,
        defaultSortField,
        defaultSortDirection);
  }

  public static final class Builder {
    private int defaultPageSize = 20;
    private int maxPageSize = 100;
    private int maxFilters = 20;
    private int maxInValues = 100;
    private boolean ignoreBlankTextFilters = true;
    private boolean ignoreEmptyCheckboxFilters = true;
    private boolean caseInsensitiveTextSearch = true;
    private SearchOperator defaultTextOperator = SearchOperator.CONTAINS;
    private String defaultSortField = "id";
    private Sort.Direction defaultSortDirection = Sort.Direction.DESC;

    private Builder() {}

    public Builder defaultPageSize(int defaultPageSize) {
      this.defaultPageSize = defaultPageSize;
      return this;
    }

    public Builder maxPageSize(int maxPageSize) {
      this.maxPageSize = maxPageSize;
      return this;
    }

    public Builder maxFilters(int maxFilters) {
      this.maxFilters = maxFilters;
      return this;
    }

    public Builder maxInValues(int maxInValues) {
      this.maxInValues = maxInValues;
      return this;
    }

    public Builder ignoreBlankTextFilters(boolean ignoreBlankTextFilters) {
      this.ignoreBlankTextFilters = ignoreBlankTextFilters;
      return this;
    }

    public Builder ignoreEmptyCheckboxFilters(boolean ignoreEmptyCheckboxFilters) {
      this.ignoreEmptyCheckboxFilters = ignoreEmptyCheckboxFilters;
      return this;
    }

    public Builder caseInsensitiveTextSearch(boolean caseInsensitiveTextSearch) {
      this.caseInsensitiveTextSearch = caseInsensitiveTextSearch;
      return this;
    }

    public Builder defaultTextOperator(SearchOperator defaultTextOperator) {
      this.defaultTextOperator = defaultTextOperator;
      return this;
    }

    public Builder defaultSortField(String defaultSortField) {
      this.defaultSortField = defaultSortField;
      return this;
    }

    public Builder defaultSortDirection(Sort.Direction defaultSortDirection) {
      this.defaultSortDirection = defaultSortDirection;
      return this;
    }

    public SearchOptions build() {
      return new SearchOptions(
          defaultPageSize,
          maxPageSize,
          maxFilters,
          maxInValues,
          ignoreBlankTextFilters,
          ignoreEmptyCheckboxFilters,
          caseInsensitiveTextSearch,
          defaultTextOperator,
          defaultSortField,
          defaultSortDirection);
    }
  }
}
