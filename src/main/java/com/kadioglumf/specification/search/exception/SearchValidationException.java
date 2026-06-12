package com.kadioglumf.specification.search.exception;

public class SearchValidationException extends RuntimeException {
  private final SearchErrorCode errorCode;

  public SearchValidationException(SearchErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public SearchErrorCode getErrorCode() {
    return errorCode;
  }
}
