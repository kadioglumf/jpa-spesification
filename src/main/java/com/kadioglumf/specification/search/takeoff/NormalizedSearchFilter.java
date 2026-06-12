package com.kadioglumf.specification.search.takeoff;

import com.kadioglumf.specification.search.definition.SearchFieldDefinition;
import com.kadioglumf.specification.search.definition.SearchOperator;

public record NormalizedSearchFilter(
    SearchFieldDefinition fieldDefinition,
    TakeoffFilterType takeoffFilterType,
    SearchOperator operator,
    Object value) {}
