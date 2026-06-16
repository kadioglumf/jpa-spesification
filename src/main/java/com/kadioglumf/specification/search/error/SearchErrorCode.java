package com.kadioglumf.specification.search.error;

import com.thy.bagstar.bagstarcore.error.code.CoreErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SearchErrorCode implements CoreErrorCode {
    MAX_PAGE_SIZE("BGSTAR_SEARCH_850", HttpStatus.BAD_REQUEST),
    TOO_MANY_FILTERS("BGSTAR_SEARCH_851", HttpStatus.BAD_REQUEST),
    EMPTY_VALUE("BGSTAR_SEARCH_852", HttpStatus.BAD_REQUEST),
    UNSUPPORTED_OPERATOR("BGSTAR_SEARCH_853", HttpStatus.BAD_REQUEST),
    TOO_MANY_IN_VALUES("BGSTAR_SEARCH_854", HttpStatus.BAD_REQUEST),
    INVALID_SORT_ORDER("BGSTAR_SEARCH_855", HttpStatus.BAD_REQUEST),
    INVALID_PAGE("BGSTAR_SEARCH_856", HttpStatus.BAD_REQUEST),
    UNKNOWN_FIELD("BGSTAR_SEARCH_857", HttpStatus.BAD_REQUEST),
    FIELD_NOT_FILTERABLE("BGSTAR_SEARCH_858", HttpStatus.BAD_REQUEST),
    FIELD_NOT_SORTABLE("BGSTAR_SEARCH_859", HttpStatus.BAD_REQUEST),
    UNSUPPORTED_FILTER_TYPE("BGSTAR_SEARCH_860", HttpStatus.BAD_REQUEST),
    INVALID_VALUE_TYPE("BGSTAR_SEARCH_861", HttpStatus.BAD_REQUEST),
    INVALID_BOOLEAN("BGSTAR_SEARCH_862", HttpStatus.BAD_REQUEST),
    INVALID_NUMBER("BGSTAR_SEARCH_863", HttpStatus.BAD_REQUEST),
    INVALID_DATE("BGSTAR_SEARCH_864", HttpStatus.BAD_REQUEST)

    ;

    private final String code;
    private final HttpStatus httpStatus;

    @Override
    public String code() {
        return code;
    }

    @Override
    public HttpStatus httpStatus() {
        return httpStatus;
    }
}
