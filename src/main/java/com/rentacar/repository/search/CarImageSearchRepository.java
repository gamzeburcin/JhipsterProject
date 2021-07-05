package com.rentacar.repository.search;

import com.rentacar.domain.CarImage;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the {@link CarImage} entity.
 */
public interface CarImageSearchRepository extends ElasticsearchRepository<CarImage, Long> {}
