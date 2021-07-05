package com.rentacar.repository.search;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure a Mock version of {@link CarSearchRepository} to test the
 * application without starting Elasticsearch.
 */
@Configuration
public class CarSearchRepositoryMockConfiguration {

    @MockBean
    private CarSearchRepository mockCarSearchRepository;
}
