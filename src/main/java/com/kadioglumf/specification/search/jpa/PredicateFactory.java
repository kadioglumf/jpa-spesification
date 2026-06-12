package com.kadioglumf.specification.search.jpa;

import com.kadioglumf.specification.search.definition.SearchFieldDefinition;
import com.kadioglumf.specification.search.definition.SearchOperator;
import com.kadioglumf.specification.search.definition.SearchOptions;
import com.kadioglumf.specification.search.exception.SearchErrorCode;
import com.kadioglumf.specification.search.exception.SearchValidationException;
import com.kadioglumf.specification.search.takeoff.NormalizedSearchFilter;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.Map;

public class PredicateFactory {
  private final PathResolver pathResolver;
  private final SearchValueConverter valueConverter;

  public PredicateFactory(PathResolver pathResolver, SearchValueConverter valueConverter) {
    this.pathResolver = pathResolver;
    this.valueConverter = valueConverter;
  }

  public Predicate create(
      Root<?> root,
      CriteriaBuilder cb,
      NormalizedSearchFilter filter,
      SearchOptions options,
      Map<String, From<?, ?>> joinCache) {
    SearchFieldDefinition definition = filter.fieldDefinition();
    Path<?> path = pathResolver.resolve(root, definition, joinCache);
    SearchOperator operator = filter.operator();
    return switch (operator) {
      case EQUAL -> cb.equal(path, valueConverter.convertSingle(definition, filter.value()));
      case NOT_EQUAL -> cb.notEqual(path, valueConverter.convertSingle(definition, filter.value()));
      case IN ->
          createInPredicate(path, valueConverter.convertList(definition, filter.value()), false);
      case NOT_IN ->
          createInPredicate(path, valueConverter.convertList(definition, filter.value()), true);
      case BETWEEN ->
          createBetweenPredicate(cb, path, valueConverter.convertRange(definition, filter.value()));
      case GREATER_THAN ->
          createComparablePredicate(
              cb, path, valueConverter.convertSingle(definition, filter.value()), operator);
      case GREATER_THAN_OR_EQUAL ->
          createComparablePredicate(
              cb, path, valueConverter.convertSingle(definition, filter.value()), operator);
      case LESS_THAN ->
          createComparablePredicate(
              cb, path, valueConverter.convertSingle(definition, filter.value()), operator);
      case LESS_THAN_OR_EQUAL ->
          createComparablePredicate(
              cb, path, valueConverter.convertSingle(definition, filter.value()), operator);
      case STARTS_WITH ->
          createLikePredicate(cb, path, definition, filter.value(), options, "", "%");
      case ENDS_WITH -> createLikePredicate(cb, path, definition, filter.value(), options, "%", "");
      case CONTAINS -> createLikePredicate(cb, path, definition, filter.value(), options, "%", "%");
      case IS_NULL -> cb.isNull(path);
      case IS_NOT_NULL -> cb.isNotNull(path);
    };
  }

  private Predicate createInPredicate(Path<?> path, List<Object> values, boolean negate) {
    Predicate in = path.in(values);
    return negate ? in.not() : in;
  }

  private Predicate createLikePredicate(
      CriteriaBuilder cb,
      Path<?> path,
      SearchFieldDefinition definition,
      Object rawValue,
      SearchOptions options,
      String prefix,
      String suffix) {
    Object converted = valueConverter.convertSingle(definition, rawValue);
    String value = prefix + converted.toString() + suffix;
    Expression<String> expression = path.as(String.class);
    if (options.caseInsensitiveTextSearch() && definition.caseInsensitive()) {
      return cb.like(cb.upper(expression), value.toUpperCase());
    }
    return cb.like(expression, value);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private Predicate createBetweenPredicate(CriteriaBuilder cb, Path<?> path, List<Object> values) {
    Comparable from = comparable(values.get(0));
    Comparable to = comparable(values.get(1));
    return cb.between((Expression<Comparable>) path, from, to);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private Predicate createComparablePredicate(
      CriteriaBuilder cb, Path<?> path, Object value, SearchOperator operator) {
    Comparable comparableValue = comparable(value);
    Expression<Comparable> expression = (Expression<Comparable>) path;
    return switch (operator) {
      case GREATER_THAN -> cb.greaterThan(expression, comparableValue);
      case GREATER_THAN_OR_EQUAL -> cb.greaterThanOrEqualTo(expression, comparableValue);
      case LESS_THAN -> cb.lessThan(expression, comparableValue);
      case LESS_THAN_OR_EQUAL -> cb.lessThanOrEqualTo(expression, comparableValue);
      default ->
          throw new SearchValidationException(
              SearchErrorCode.UNSUPPORTED_OPERATOR, "Unsupported operator for field.");
    };
  }

  private Comparable<?> comparable(Object value) {
    if (value instanceof Comparable<?> comparable) {
      return comparable;
    }
    throw new SearchValidationException(
        SearchErrorCode.INVALID_VALUE_TYPE, "Comparable filter value is required.");
  }
}
