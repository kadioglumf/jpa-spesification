package com.kadioglumf.specification.search.jpa;

import com.kadioglumf.specification.search.definition.SearchFieldDefinition;
import com.kadioglumf.specification.search.definition.SearchFieldRegistry;
import com.kadioglumf.specification.search.definition.SearchOptions;
import com.kadioglumf.specification.search.exception.SearchErrorCode;
import com.kadioglumf.specification.search.exception.SearchValidationException;
import com.kadioglumf.specification.search.takeoff.TakeoffTableRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class SearchPageableFactory {
  public Pageable create(TakeoffTableRequest request, SearchFieldRegistry registry) {
    if (request == null) {
      throw new SearchValidationException(
          SearchErrorCode.EMPTY_VALUE, "Search request is required.");
    }
    SearchOptions options = registry.options();
    int currentPage = validateCurrentPage(request.currentPage());
    int rowsPerPage = validateRowsPerPage(request.rowsPerPage(), options);
    Sort sort = createSort(request, registry);
    return PageRequest.of(currentPage - 1, rowsPerPage, sort);
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

  private Sort createSort(TakeoffTableRequest request, SearchFieldRegistry registry) {
    String sortField = request.sortField();
    SearchOptions options = registry.options();
    if (sortField == null || sortField.isBlank()) {
      if (options.defaultSortField() == null || options.defaultSortField().isBlank()) {
        return Sort.unsorted();
      }
      SearchFieldDefinition definition = registry.requireSortable(options.defaultSortField());
      return Sort.by(options.defaultSortDirection(), definition.entityPath());
    }
    SearchFieldDefinition definition = registry.requireSortable(sortField);
    Sort.Direction direction = parseDirection(request.sortOrder());
    return Sort.by(direction, definition.entityPath());
  }

  private Sort.Direction parseDirection(String sortOrder) {
    if (sortOrder == null || sortOrder.isBlank()) {
      return Sort.Direction.ASC;
    }
    if ("asc".equalsIgnoreCase(sortOrder)) {
      return Sort.Direction.ASC;
    }
    if ("desc".equalsIgnoreCase(sortOrder)) {
      return Sort.Direction.DESC;
    }
    throw new SearchValidationException(SearchErrorCode.INVALID_SORT_ORDER, "Invalid sort order.");
  }
}
