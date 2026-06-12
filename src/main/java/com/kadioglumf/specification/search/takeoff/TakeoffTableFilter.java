package com.kadioglumf.specification.search.takeoff;

import jakarta.validation.constraints.NotBlank;

public record TakeoffTableFilter(@NotBlank String field, String type, Object value) {}
