package com.rentacar.repository.search;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure a Mock version of {@link RentalSearchRepository} to test the
 * application without starting Elasticsearch.
 */
@Configuration
public class RentalSearchRepositoryMockConfiguration {

    @MockBean
    private RentalSearchRepository mockRentalSearchRepository;
}
