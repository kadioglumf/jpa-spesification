package com.kadioglumf.specification.search.jpa;

import com.kadioglumf.specification.search.definition.SearchFieldRegistry;
import com.kadioglumf.specification.search.definition.SearchOptions;
import com.kadioglumf.specification.search.exception.SearchErrorCode;
import com.kadioglumf.specification.search.exception.SearchValidationException;
import com.kadioglumf.specification.search.takeoff.TakeoffTableRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class SearchPageableFactory {
  public Pageable create(TakeoffTableRequest request, SearchFieldRegistry registry) {
    if (request == null) {
      throw new SearchValidationException(
          SearchErrorCode.EMPTY_VALUE, "Search request is required.");
    }
    SearchOptions options = registry.options();
    int currentPage = validateCurrentPage(request.currentPage());
    int rowsPerPage = validateRowsPerPage(request.rowsPerPage(), options);
    return PageRequest.of(currentPage - 1, rowsPerPage);
  }

  private int validateCurrentPage(Integer currentPage) {
    if (currentPage == null || currentPage < 1) {
      throw new SearchValidationException(
          SearchErrorCode.INVALID_PAGE, "currentPage must be greater than zero.");
    }
    return currentPage;
  }

  private int validateRowsPerPage(Integer rowsPerPage, SearchOptions options) {
    if (rowsPerPage == null || rowsPerPage < 1) {
      throw new SearchValidationException(
          SearchErrorCode.INVALID_PAGE_SIZE, "rowsPerPage must be greater than zero.");
    }
    if (rowsPerPage > options.maxPageSize()) {
      throw new SearchValidationException(
          SearchErrorCode.INVALID_PAGE_SIZE, "rowsPerPage exceeds maximum page size.");
    }
    return rowsPerPage;
  }
}
