package com.kadioglumf.specification.search.autoconfigure;

import com.kadioglumf.specification.search.jpa.JoinResolver;
import com.kadioglumf.specification.search.jpa.PathResolver;
import com.kadioglumf.specification.search.jpa.PredicateFactory;
import com.kadioglumf.specification.search.jpa.SearchPageableFactory;
import com.kadioglumf.specification.search.jpa.SearchSpecificationBuilder;
import com.kadioglumf.specification.search.jpa.SearchValueConverter;
import com.kadioglumf.specification.search.takeoff.TakeoffSearchRequestParser;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class TakeoffJpaSpecificationAutoConfiguration {
  @Bean
  @ConditionalOnMissingBean
  public TakeoffSearchRequestParser takeoffSearchRequestParser() {
    return new TakeoffSearchRequestParser();
  }

  @Bean
  @ConditionalOnMissingBean
  public SearchValueConverter searchValueConverter() {
    return new SearchValueConverter();
  }

  @Bean
  @ConditionalOnMissingBean
  public JoinResolver joinResolver() {
    return new JoinResolver();
  }

  @Bean
  @ConditionalOnMissingBean
  public PathResolver pathResolver(JoinResolver joinResolver) {
    return new PathResolver(joinResolver);
  }

  @Bean
  @ConditionalOnMissingBean
  public PredicateFactory predicateFactory(
      PathResolver pathResolver, SearchValueConverter searchValueConverter) {
    return new PredicateFactory(pathResolver, searchValueConverter);
  }

  @Bean
  @ConditionalOnMissingBean
  public SearchSpecificationBuilder searchSpecificationBuilder(
      TakeoffSearchRequestParser requestParser,
      PredicateFactory predicateFactory,
      JoinResolver joinResolver) {
    return new SearchSpecificationBuilder(requestParser, predicateFactory, joinResolver);
  }

  @Bean
  @ConditionalOnMissingBean
  public SearchPageableFactory searchPageableFactory() {
    return new SearchPageableFactory();
  }
}
