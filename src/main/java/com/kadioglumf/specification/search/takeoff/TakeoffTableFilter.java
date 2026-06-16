package com.kadioglumf.specification.search.takeoff;

import com.thy.bagstar.bagstarcore.error.code.ValidationMessageConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TakeoffTableFilter(
        @NotBlank(message = ValidationMessageConstants.CANNOT_BE_BLANK)
        String field,
        @NotBlank(message = ValidationMessageConstants.CANNOT_BE_BLANK)
        String type,
        @NotNull(message = ValidationMessageConstants.CANNOT_BE_NULL)
        Object value) {}
