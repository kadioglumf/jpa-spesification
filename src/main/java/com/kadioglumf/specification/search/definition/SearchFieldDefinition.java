package com.kadioglumf.specification.search.definition;

import com.kadioglumf.specification.search.takeoff.TakeoffFilterType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface SearchFieldDefinition {
  String apiFieldName();

  String entityPath();

  SearchFieldType fieldType();

  Set<SearchOperator> allowedOperators();

  boolean filterable();

  boolean sortable();

  default boolean caseInsensitive() {
    return true;
  }

  default List<SearchJoinDefinition> joins() {
    return List.of();
  }

  default boolean distinctRequired() {
    return false;
  }

  default Map<TakeoffFilterType, SearchOperator> defaultOperatorOverrides() {
    return Map.of();
  }

  default Optional<Class<? extends Enum<?>>> enumClass() {
    return Optional.empty();
  }
}
