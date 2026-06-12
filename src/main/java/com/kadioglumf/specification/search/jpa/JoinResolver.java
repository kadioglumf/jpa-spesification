package com.kadioglumf.specification.search.jpa;

import com.kadioglumf.specification.search.definition.SearchFieldDefinition;
import com.kadioglumf.specification.search.definition.SearchJoinDefinition;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import java.util.HashMap;
import java.util.Map;

public class JoinResolver {
  public From<?, ?> resolve(
      Root<?> root,
      SearchFieldDefinition definition,
      String[] pathSegments,
      Map<String, From<?, ?>> joinCache) {
    From<?, ?> current = root;
    StringBuilder currentPath = new StringBuilder();
    for (int i = 0; i < pathSegments.length - 1; i++) {
      if (!currentPath.isEmpty()) {
        currentPath.append('.');
      }
      currentPath.append(pathSegments[i]);
      String joinPath = currentPath.toString();
      From<?, ?> parent = current;
      String attribute = pathSegments[i];
      current =
          joinCache.computeIfAbsent(
              joinPath, ignored -> join(parent, attribute, joinType(definition, joinPath)));
    }
    return current;
  }

  public Map<String, From<?, ?>> newJoinCache() {
    return new HashMap<>();
  }

  private JoinType joinType(SearchFieldDefinition definition, String path) {
    return definition.joins().stream()
        .filter(joinDefinition -> joinDefinition.path().equals(path))
        .map(SearchJoinDefinition::joinType)
        .findFirst()
        .orElse(JoinType.LEFT);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private Join<?, ?> join(From<?, ?> from, String attribute, JoinType joinType) {
    return ((From) from).join(attribute, joinType);
  }
}
