package com.rentacar.repository.search;

import com.rentacar.domain.Color;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the {@link Color} entity.
 */
public interface ColorSearchRepository extends ElasticsearchRepository<Color, Long> {}
