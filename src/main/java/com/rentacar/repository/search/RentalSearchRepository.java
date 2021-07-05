package com.rentacar.repository.search;

import com.rentacar.domain.Rental;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the {@link Rental} entity.
 */
public interface RentalSearchRepository extends ElasticsearchRepository<Rental, Long> {}
