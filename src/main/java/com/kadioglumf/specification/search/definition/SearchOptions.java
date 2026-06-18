package com.kadioglumf.specification.search.definition;

public record SearchOptions(
    int defaultPageSize,
    int maxPageSize,
    int maxFilters,
    int maxInValues,
    boolean ignoreBlankTextFilters,
    boolean caseInsensitiveTextSearch) {
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
  }

  public static SearchOptions defaults() {
    return new SearchOptions(20, 100, 20, 100, true, true);
  }
}
