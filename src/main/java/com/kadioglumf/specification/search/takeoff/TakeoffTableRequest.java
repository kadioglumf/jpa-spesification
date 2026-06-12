package com.kadioglumf.specification.search.takeoff;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;

public record TakeoffTableRequest(
    @Min(1) Integer currentPage,
    @Min(1) Integer rowsPerPage,
    String sortField,
    String sortOrder,
    @Valid List<TakeoffTableFilter> filters) {}
