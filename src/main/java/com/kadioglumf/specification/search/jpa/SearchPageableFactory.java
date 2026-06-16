package com.kadioglumf.specification.search.jpa;

import com.kadioglumf.specification.search.definition.SearchFieldRegistry;
import com.kadioglumf.specification.search.definition.SearchOptions;
import com.kadioglumf.specification.search.error.SearchErrorCode;
import com.kadioglumf.specification.search.takeoff.TakeoffTableRequest;
import com.thy.bagstar.bagstarcore.error.exception.BusinessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class SearchPageableFactory {
  public Pageable create(TakeoffTableRequest request, SearchFieldRegistry registry) {
    if (request == null) {
      throw new BusinessException(SearchErrorCode.INVALID_REQUEST);
    }
    SearchOptions options = registry.options();
    int currentPage = request.currentPage();
    int rowsPerPage = validateRowsPerPage(request.rowsPerPage(), options);
    return PageRequest.of(currentPage - 1, rowsPerPage);
  }

  private int validateRowsPerPage(Integer rowsPerPage, SearchOptions options) {
    if (rowsPerPage > options.maxPageSize()) {
      throw new BusinessException(SearchErrorCode.MAX_PAGE_SIZE, options.maxPageSize());
    }
    return rowsPerPage;
  }
}
