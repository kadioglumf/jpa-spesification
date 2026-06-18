package com.kadioglumf.specification.search.jpa;

import com.kadioglumf.specification.search.definition.SearchFetchDefinition;
import com.kadioglumf.specification.search.definition.SearchFieldRegistry;
import com.kadioglumf.specification.search.error.SearchErrorCode;
import com.kadioglumf.specification.search.takeoff.NormalizedSearchFilter;
import com.kadioglumf.specification.search.takeoff.TakeoffSearchRequestParser;
import com.kadioglumf.specification.search.takeoff.TakeoffTableRequest;
import com.kadioglumf.specification.search.takeoff.TakeoffTableSort;
import com.thy.bagstar.bagstarcore.error.exception.BusinessException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.FetchParent;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

public class SearchSpecificationBuilder {
  private final TakeoffSearchRequestParser requestParser;
  private final PredicateFactory predicateFactory;
  private final JoinResolver joinResolver;
  private final PathResolver pathResolver;

  public SearchSpecificationBuilder(
      TakeoffSearchRequestParser requestParser,
      PredicateFactory predicateFactory,
      JoinResolver joinResolver,
      PathResolver pathResolver) {
    this.requestParser = requestParser;
    this.predicateFactory = predicateFactory;
    this.joinResolver = joinResolver;
    this.pathResolver = pathResolver;
  }

  public <T> Specification<T> build(TakeoffTableRequest request, SearchFieldRegistry registry) {
    List<NormalizedSearchFilter> filters = requestParser.parseFilters(request, registry);
    return (root, query, cb) -> {
      Map<String, From<?, ?>> joinCache = joinResolver.newJoinCache();
      boolean countQuery = isCountQuery(query);
      if (!countQuery) {
        applyFetches(root, registry, joinCache);
      }
      List<Predicate> predicates =
          filters.stream()
              .map(
                  filter ->
                      predicateFactory.create(root, cb, filter, registry.options(), joinCache))
              .toList();
      if (shouldApplyDistinct(filters, registry)) {
        query.distinct(true);
      }
      if (!countQuery) {
        applySort(root, query, cb, registry, request, joinCache);
      }
      return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(Predicate[]::new));
    };
  }

  private boolean isCountQuery(CriteriaQuery<?> query) {
    Class<?> resultType = query.getResultType();
    return Long.class.equals(resultType) || long.class.equals(resultType);
  }

  private boolean shouldApplyDistinct(
      List<NormalizedSearchFilter> filters, SearchFieldRegistry registry) {
    return filters.stream().anyMatch(filter -> filter.fieldDefinition().distinctRequired())
        || registry.fetches().stream().anyMatch(SearchFetchDefinition::distinctRequired);
  }

  private void applySort(
      Root<?> root,
      CriteriaQuery<?> query,
      CriteriaBuilder cb,
      SearchFieldRegistry registry,
      TakeoffTableRequest request,
      Map<String, From<?, ?>> joinCache) {
    List<Order> orders = new ArrayList<>();
    if (request.sorts() != null && !request.sorts().isEmpty()) {
      for (TakeoffTableSort sort : request.sorts()) {
        orders.add(createOrder(root, cb, registry, joinCache, sort));
      }
    } else {
      Order order = createLegacyOrder(root, cb, registry, request, joinCache);
      if (order != null) {
        orders.add(order);
      }
    }

    if (!orders.isEmpty()) {
      query.orderBy(orders);
    }
  }

  private Order createLegacyOrder(
      Root<?> root,
      CriteriaBuilder cb,
      SearchFieldRegistry registry,
      TakeoffTableRequest request,
      Map<String, From<?, ?>> joinCache) {
    String sortField = request.sortField();
    if (sortField == null || sortField.isBlank()) {
      return null;
    }

    return createOrder(root, cb, registry, joinCache, sortField, parseDirection(request.sortOrder()));
  }

  private Order createOrder(
      Root<?> root,
      CriteriaBuilder cb,
      SearchFieldRegistry registry,
      Map<String, From<?, ?>> joinCache,
      TakeoffTableSort sort) {
    if (sort == null) {
      throw new BusinessException(SearchErrorCode.INVALID_SORT_ORDER);
    }
    return createOrder(root, cb, registry, joinCache, sort.field(), parseDirection(sort.order()));
  }

  private Order createOrder(
      Root<?> root,
      CriteriaBuilder cb,
      SearchFieldRegistry registry,
      Map<String, From<?, ?>> joinCache,
      String sortField,
      Sort.Direction direction) {
    var definition = registry.requireSortable(sortField);
    return direction.isAscending()
        ? cb.asc(pathResolver.resolve(root, definition, joinCache))
        : cb.desc(pathResolver.resolve(root, definition, joinCache));
  }

  private Sort.Direction parseDirection(String sortOrder) {
    if (sortOrder == null || sortOrder.isBlank()) {
      return Sort.Direction.ASC;
    }
    return switch (sortOrder.trim().toLowerCase(Locale.ROOT)) {
      case "asc" -> Sort.Direction.ASC;
      case "desc" -> Sort.Direction.DESC;
      default ->
          throw new BusinessException(SearchErrorCode.INVALID_SORT_ORDER);
    };
  }

  private void applyFetches(
      Root<?> root, SearchFieldRegistry registry, Map<String, From<?, ?>> joinCache) {
    if (registry.fetches().isEmpty()) {
      return;
    }
    Map<String, FetchParent<?, ?>> fetchCache = new java.util.HashMap<>();
    for (SearchFetchDefinition fetchDefinition : registry.fetches()) {
      applyFetch(root, fetchDefinition, fetchCache, joinCache);
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void applyFetch(
      Root<?> root,
      SearchFetchDefinition fetchDefinition,
      Map<String, FetchParent<?, ?>> fetchCache,
      Map<String, From<?, ?>> joinCache) {
    FetchParent current = root;
    StringBuilder currentPath = new StringBuilder();
    for (String segment : fetchDefinition.path().split("\\.")) {
      if (!currentPath.isEmpty()) {
        currentPath.append('.');
      }
      currentPath.append(segment);
      String fetchPath = currentPath.toString();
      FetchParent parent = current;
      Fetch<?, ?> fetch =
          (Fetch<?, ?>)
              fetchCache.computeIfAbsent(
                  fetchPath, ignored -> parent.fetch(segment, fetchDefinition.joinType()));
      if (fetch instanceof From<?, ?> from) {
        joinCache.putIfAbsent(fetchPath, from);
      }
      current =
          fetch instanceof FetchParent<?, ?> fetchParent
              ? (FetchParent) fetchParent
              : (FetchParent) fetchCache.get(fetchPath);
    }
  }
}
