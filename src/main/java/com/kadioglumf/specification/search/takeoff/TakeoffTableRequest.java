package com.kadioglumf.specification.search.takeoff;

import com.thy.bagstar.bagstarcore.error.code.ValidationMessageConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TakeoffTableRequest(
        @Min(1)
        @NotNull(message = ValidationMessageConstants.CANNOT_BE_NULL)
        Integer currentPage,

        @Min(1)
        @NotNull(message = ValidationMessageConstants.CANNOT_BE_NULL)
        Integer rowsPerPage,

        String sortField,
        String sortOrder,

        @Valid
        List<TakeoffTableSort> sorts,

        @Valid
        List<TakeoffTableFilter> filters) {}
