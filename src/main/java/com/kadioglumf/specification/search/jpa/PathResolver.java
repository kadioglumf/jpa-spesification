package com.kadioglumf.specification.search.jpa;

import com.kadioglumf.specification.search.definition.SearchFieldDefinition;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import java.util.Map;

public class PathResolver {
  private final JoinResolver joinResolver;

  public PathResolver(JoinResolver joinResolver) {
    this.joinResolver = joinResolver;
  }

  public Path<?> resolve(
      Root<?> root, SearchFieldDefinition definition, Map<String, From<?, ?>> joinCache) {
    String[] pathSegments = definition.entityPath().split("\\.");
    if (pathSegments.length == 1) {
      return root.get(pathSegments[0]);
    }
    From<?, ?> from = joinResolver.resolve(root, definition, pathSegments, joinCache);
    return from.get(pathSegments[pathSegments.length - 1]);
  }
}
