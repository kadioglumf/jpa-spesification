package com.kadioglumf.specification.search.definition;

import jakarta.persistence.criteria.JoinType;

public record SearchJoinDefinition(String path, JoinType joinType) {
  public SearchJoinDefinition {
    if (joinType == null) {
      joinType = JoinType.INNER;
    }
  }

  public static SearchJoinDefinition left(String path) {
    return new SearchJoinDefinition(path, JoinType.LEFT);
  }

  public static SearchJoinDefinition inner(String path) {
    return new SearchJoinDefinition(path, JoinType.INNER);
  }
}
