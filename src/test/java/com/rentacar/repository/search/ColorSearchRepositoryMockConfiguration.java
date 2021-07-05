package com.rentacar.repository.search;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure a Mock version of {@link ColorSearchRepository} to test the
 * application without starting Elasticsearch.
 */
@Configuration
public class ColorSearchRepositoryMockConfiguration {

    @MockBean
    private ColorSearchRepository mockColorSearchRepository;
}
