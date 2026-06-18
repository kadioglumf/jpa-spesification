package com.kadioglumf.specification.search.definition;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SearchFieldDefinition {
  String apiFieldName();

  String entityPath();

  default SearchFieldType fieldType() {
    return SearchFieldType.STRING;
  }

  Set<SearchOperator> allowedOperators();

  default boolean filterable() {
    return true;
  }

  default boolean sortable() {
    return true;
  }

  default boolean caseInsensitive() {
    return true;
  }

  default List<SearchJoinDefinition> joins() {
    return List.of();
  }

  default boolean distinctRequired() {
    return false;
  }

  default Optional<Class<? extends Enum<?>>> enumClass() {
    return Optional.empty();
  }
}
