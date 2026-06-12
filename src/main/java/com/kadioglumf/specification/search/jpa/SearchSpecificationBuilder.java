package com.kadioglumf.specification.search.jpa;

import com.kadioglumf.specification.search.definition.SearchFieldRegistry;
import com.kadioglumf.specification.search.takeoff.NormalizedSearchFilter;
import com.kadioglumf.specification.search.takeoff.TakeoffSearchRequestParser;
import com.kadioglumf.specification.search.takeoff.TakeoffTableRequest;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.domain.Specification;

public class SearchSpecificationBuilder {
  private final TakeoffSearchRequestParser requestParser;
  private final PredicateFactory predicateFactory;
  private final JoinResolver joinResolver;

  public SearchSpecificationBuilder(
      TakeoffSearchRequestParser requestParser,
      PredicateFactory predicateFactory,
      JoinResolver joinResolver) {
    this.requestParser = requestParser;
    this.predicateFactory = predicateFactory;
    this.joinResolver = joinResolver;
  }

  public <T> Specification<T> build(TakeoffTableRequest request, SearchFieldRegistry registry) {
    List<NormalizedSearchFilter> filters = requestParser.parseFilters(request, registry);
    return (root, query, cb) -> {
      Map<String, From<?, ?>> joinCache = joinResolver.newJoinCache();
      List<Predicate> predicates =
          filters.stream()
              .map(
                  filter ->
                      predicateFactory.create(root, cb, filter, registry.options(), joinCache))
              .toList();
      if (filters.stream().anyMatch(filter -> filter.fieldDefinition().distinctRequired())) {
        query.distinct(true);
      }
      return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(Predicate[]::new));
    };
  }
}
