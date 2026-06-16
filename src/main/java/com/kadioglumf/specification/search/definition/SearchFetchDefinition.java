package com.kadioglumf.specification.search.definition;

import jakarta.persistence.criteria.JoinType;

public record SearchFetchDefinition(String path, JoinType joinType, boolean distinctRequired) {
  public SearchFetchDefinition {
    if (joinType == null) {
      joinType = JoinType.LEFT;
    }
  }

  public static SearchFetchDefinition left(String path) {
    return new SearchFetchDefinition(path, JoinType.LEFT, false);
  }

  public static SearchFetchDefinition inner(String path) {
    return new SearchFetchDefinition(path, JoinType.INNER, false);
  }

  public static SearchFetchDefinition leftDistinct(String path) {
    return new SearchFetchDefinition(path, JoinType.LEFT, true);
  }
}
