package com.kadioglumf.specification.search.takeoff;

import java.util.List;
import org.springframework.data.domain.Page;

public record TakeoffTableResponse<T>(
    List<T> data, long totalItem, int currentPage, int rowsPerPage, int totalPages) {
  public static <T> TakeoffTableResponse<T> from(Page<T> page) {
    return new TakeoffTableResponse<>(
        page.getContent(),
        page.getTotalElements(),
        page.getNumber() + 1,
        page.getSize(),
        page.getTotalPages());
  }
}
