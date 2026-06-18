package com.kadioglumf.specification.search.takeoff;

import com.thy.bagstar.bagstarcore.error.code.ValidationMessageConstants;
import jakarta.validation.constraints.NotBlank;

public record TakeoffTableSort(
    @NotBlank(message = ValidationMessageConstants.CANNOT_BE_BLANK) String field, String order) {}
