package com.rentacar.web.rest;

import static com.rentacar.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.rentacar.IntegrationTest;
import com.rentacar.domain.Rental;
import com.rentacar.repository.RentalRepository;
import com.rentacar.repository.search.RentalSearchRepository;
import com.rentacar.service.criteria.RentalCriteria;
import com.rentacar.service.dto.RentalDTO;
import com.rentacar.service.mapper.RentalMapper;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link RentalResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class RentalResourceIT {

    private static final ZonedDateTime DEFAULT_RENT_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_RENT_DATE = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final ZonedDateTime SMALLER_RENT_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(-1L), ZoneOffset.UTC);

    private static final ZonedDateTime DEFAULT_RETURN_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_RETURN_DATE = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final ZonedDateTime SMALLER_RETURN_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(-1L), ZoneOffset.UTC);

    private static final Long DEFAULT_CUSTOMER_ID = 1L;
    private static final Long UPDATED_CUSTOMER_ID = 2L;
    private static final Long SMALLER_CUSTOMER_ID = 1L - 1L;

    private static final Long DEFAULT_CAR_ID = 1L;
    private static final Long UPDATED_CAR_ID = 2L;
    private static final Long SMALLER_CAR_ID = 1L - 1L;

    private static final String ENTITY_API_URL = "/api/rentals";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/rentals";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private RentalMapper rentalMapper;

    /**
     * This repository is mocked in the com.rentacar.repository.search test package.
     *
     * @see com.rentacar.repository.search.RentalSearchRepositoryMockConfiguration
     */
    @Autowired
    private RentalSearchRepository mockRentalSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restRentalMockMvc;

    private Rental rental;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Rental createEntity(EntityManager em) {
        Rental rental = new Rental()
            .rentDate(DEFAULT_RENT_DATE)
            .returnDate(DEFAULT_RETURN_DATE)
            .customerId(DEFAULT_CUSTOMER_ID)
            .carId(DEFAULT_CAR_ID);
        return rental;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Rental createUpdatedEntity(EntityManager em) {
        Rental rental = new Rental()
            .rentDate(UPDATED_RENT_DATE)
            .returnDate(UPDATED_RETURN_DATE)
            .customerId(UPDATED_CUSTOMER_ID)
            .carId(UPDATED_CAR_ID);
        return rental;
    }

    @BeforeEach
    public void initTest() {
        rental = createEntity(em);
    }

    @Test
    @Transactional
    void createRental() throws Exception {
        int databaseSizeBeforeCreate = rentalRepository.findAll().size();
        // Create the Rental
        RentalDTO rentalDTO = rentalMapper.toDto(rental);
        restRentalMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(rentalDTO)))
            .andExpect(status().isCreated());

        // Validate the Rental in the database
        List<Rental> rentalList = rentalRepository.findAll();
        assertThat(rentalList).hasSize(databaseSizeBeforeCreate + 1);
        Rental testRental = rentalList.get(rentalList.size() - 1);
        assertThat(testRental.getRentDate()).isEqualTo(DEFAULT_RENT_DATE);
        assertThat(testRental.getReturnDate()).isEqualTo(DEFAULT_RETURN_DATE);
        assertThat(testRental.getCustomerId()).isEqualTo(DEFAULT_CUSTOMER_ID);
        assertThat(testRental.getCarId()).isEqualTo(DEFAULT_CAR_ID);

        // Validate the Rental in Elasticsearch
        verify(mockRentalSearchRepository, times(1)).save(testRental);
    }

    @Test
    @Transactional
    void createRentalWithExistingId() throws Exception {
        // Create the Rental with an existing ID
        rental.setId(1L);
        RentalDTO rentalDTO = rentalMapper.toDto(rental);

        int databaseSizeBeforeCreate = rentalRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restRentalMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(rentalDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Rental in the database
        List<Rental> rentalList = rentalRepository.findAll();
        assertThat(rentalList).hasSize(databaseSizeBeforeCreate);

        // Validate the Rental in Elasticsearch
        verify(mockRentalSearchRepository, times(0)).save(rental);
    }

    @Test
    @Transactional
    void getAllRentals() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList
        restRentalMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(rental.getId().intValue())))
            .andExpect(jsonPath("$.[*].rentDate").value(hasItem(sameInstant(DEFAULT_RENT_DATE))))
            .andExpect(jsonPath("$.[*].returnDate").value(hasItem(sameInstant(DEFAULT_RETURN_DATE))))
            .andExpect(jsonPath("$.[*].customerId").value(hasItem(DEFAULT_CUSTOMER_ID.intValue())))
            .andExpect(jsonPath("$.[*].carId").value(hasItem(DEFAULT_CAR_ID.intValue())));
    }

    @Test
    @Transactional
    void getRental() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get the rental
        restRentalMockMvc
            .perform(get(ENTITY_API_URL_ID, rental.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(rental.getId().intValue()))
            .andExpect(jsonPath("$.rentDate").value(sameInstant(DEFAULT_RENT_DATE)))
            .andExpect(jsonPath("$.returnDate").value(sameInstant(DEFAULT_RETURN_DATE)))
            .andExpect(jsonPath("$.customerId").value(DEFAULT_CUSTOMER_ID.intValue()))
            .andExpect(jsonPath("$.carId").value(DEFAULT_CAR_ID.intValue()));
    }

    @Test
    @Transactional
    void getRentalsByIdFiltering() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        Long id = rental.getId();

        defaultRentalShouldBeFound("id.equals=" + id);
        defaultRentalShouldNotBeFound("id.notEquals=" + id);

        defaultRentalShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultRentalShouldNotBeFound("id.greaterThan=" + id);

        defaultRentalShouldBeFound("id.lessThanOrEqual=" + id);
        defaultRentalShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllRentalsByRentDateIsEqualToSomething() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where rentDate equals to DEFAULT_RENT_DATE
        defaultRentalShouldBeFound("rentDate.equals=" + DEFAULT_RENT_DATE);

        // Get all the rentalList where rentDate equals to UPDATED_RENT_DATE
        defaultRentalShouldNotBeFound("rentDate.equals=" + UPDATED_RENT_DATE);
    }

    @Test
    @Transactional
    void getAllRentalsByRentDateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where rentDate not equals to DEFAULT_RENT_DATE
        defaultRentalShouldNotBeFound("rentDate.notEquals=" + DEFAULT_RENT_DATE);

        // Get all the rentalList where rentDate not equals to UPDATED_RENT_DATE
        defaultRentalShouldBeFound("rentDate.notEquals=" + UPDATED_RENT_DATE);
    }

    @Test
    @Transactional
    void getAllRentalsByRentDateIsInShouldWork() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where rentDate in DEFAULT_RENT_DATE or UPDATED_RENT_DATE
        defaultRentalShouldBeFound("rentDate.in=" + DEFAULT_RENT_DATE + "," + UPDATED_RENT_DATE);

        // Get all the rentalList where rentDate equals to UPDATED_RENT_DATE
        defaultRentalShouldNotBeFound("rentDate.in=" + UPDATED_RENT_DATE);
    }

    @Test
    @Transactional
    void getAllRentalsByRentDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where rentDate is not null
        defaultRentalShouldBeFound("rentDate.specified=true");

        // Get all the rentalList where rentDate is null
        defaultRentalShouldNotBeFound("rentDate.specified=false");
    }

    @Test
    @Transactional
    void getAllRentalsByRentDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where rentDate is greater than or equal to DEFAULT_RENT_DATE
        defaultRentalShouldBeFound("rentDate.greaterThanOrEqual=" + DEFAULT_RENT_DATE);

        // Get all the rentalList where rentDate is greater than or equal to UPDATED_RENT_DATE
        defaultRentalShouldNotBeFound("rentDate.greaterThanOrEqual=" + UPDATED_RENT_DATE);
    }

    @Test
    @Transactional
    void getAllRentalsByRentDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where rentDate is less than or equal to DEFAULT_RENT_DATE
        defaultRentalShouldBeFound("rentDate.lessThanOrEqual=" + DEFAULT_RENT_DATE);

        // Get all the rentalList where rentDate is less than or equal to SMALLER_RENT_DATE
        defaultRentalShouldNotBeFound("rentDate.lessThanOrEqual=" + SMALLER_RENT_DATE);
    }

    @Test
    @Transactional
    void getAllRentalsByRentDateIsLessThanSomething() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where rentDate is less than DEFAULT_RENT_DATE
        defaultRentalShouldNotBeFound("rentDate.lessThan=" + DEFAULT_RENT_DATE);

        // Get all the rentalList where rentDate is less than UPDATED_RENT_DATE
        defaultRentalShouldBeFound("rentDate.lessThan=" + UPDATED_RENT_DATE);
    }

    @Test
    @Transactional
    void getAllRentalsByRentDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where rentDate is greater than DEFAULT_RENT_DATE
        defaultRentalShouldNotBeFound("rentDate.greaterThan=" + DEFAULT_RENT_DATE);

        // Get all the rentalList where rentDate is greater than SMALLER_RENT_DATE
        defaultRentalShouldBeFound("rentDate.greaterThan=" + SMALLER_RENT_DATE);
    }

    @Test
    @Transactional
    void getAllRentalsByReturnDateIsEqualToSomething() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where returnDate equals to DEFAULT_RETURN_DATE
        defaultRentalShouldBeFound("returnDate.equals=" + DEFAULT_RETURN_DATE);

        // Get all the rentalList where returnDate equals to UPDATED_RETURN_DATE
        defaultRentalShouldNotBeFound("returnDate.equals=" + UPDATED_RETURN_DATE);
    }

    @Test
    @Transactional
    void getAllRentalsByReturnDateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where returnDate not equals to DEFAULT_RETURN_DATE
        defaultRentalShouldNotBeFound("returnDate.notEquals=" + DEFAULT_RETURN_DATE);

        // Get all the rentalList where returnDate not equals to UPDATED_RETURN_DATE
        defaultRentalShouldBeFound("returnDate.notEquals=" + UPDATED_RETURN_DATE);
    }

    @Test
    @Transactional
    void getAllRentalsByReturnDateIsInShouldWork() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where returnDate in DEFAULT_RETURN_DATE or UPDATED_RETURN_DATE
        defaultRentalShouldBeFound("returnDate.in=" + DEFAULT_RETURN_DATE + "," + UPDATED_RETURN_DATE);

        // Get all the rentalList where returnDate equals to UPDATED_RETURN_DATE
        defaultRentalShouldNotBeFound("returnDate.in=" + UPDATED_RETURN_DATE);
    }

    @Test
    @Transactional
    void getAllRentalsByReturnDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where returnDate is not null
        defaultRentalShouldBeFound("returnDate.specified=true");

        // Get all the rentalList where returnDate is null
        defaultRentalShouldNotBeFound("returnDate.specified=false");
    }

    @Test
    @Transactional
    void getAllRentalsByReturnDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where returnDate is greater than or equal to DEFAULT_RETURN_DATE
        defaultRentalShouldBeFound("returnDate.greaterThanOrEqual=" + DEFAULT_RETURN_DATE);

        // Get all the rentalList where returnDate is greater than or equal to UPDATED_RETURN_DATE
        defaultRentalShouldNotBeFound("returnDate.greaterThanOrEqual=" + UPDATED_RETURN_DATE);
    }

    @Test
    @Transactional
    void getAllRentalsByReturnDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where returnDate is less than or equal to DEFAULT_RETURN_DATE
        defaultRentalShouldBeFound("returnDate.lessThanOrEqual=" + DEFAULT_RETURN_DATE);

        // Get all the rentalList where returnDate is less than or equal to SMALLER_RETURN_DATE
        defaultRentalShouldNotBeFound("returnDate.lessThanOrEqual=" + SMALLER_RETURN_DATE);
    }

    @Test
    @Transactional
    void getAllRentalsByReturnDateIsLessThanSomething() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where returnDate is less than DEFAULT_RETURN_DATE
        defaultRentalShouldNotBeFound("returnDate.lessThan=" + DEFAULT_RETURN_DATE);

        // Get all the rentalList where returnDate is less than UPDATED_RETURN_DATE
        defaultRentalShouldBeFound("returnDate.lessThan=" + UPDATED_RETURN_DATE);
    }

    @Test
    @Transactional
    void getAllRentalsByReturnDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where returnDate is greater than DEFAULT_RETURN_DATE
        defaultRentalShouldNotBeFound("returnDate.greaterThan=" + DEFAULT_RETURN_DATE);

        // Get all the rentalList where returnDate is greater than SMALLER_RETURN_DATE
        defaultRentalShouldBeFound("returnDate.greaterThan=" + SMALLER_RETURN_DATE);
    }

    @Test
    @Transactional
    void getAllRentalsByCustomerIdIsEqualToSomething() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where customerId equals to DEFAULT_CUSTOMER_ID
        defaultRentalShouldBeFound("customerId.equals=" + DEFAULT_CUSTOMER_ID);

        // Get all the rentalList where customerId equals to UPDATED_CUSTOMER_ID
        defaultRentalShouldNotBeFound("customerId.equals=" + UPDATED_CUSTOMER_ID);
    }

    @Test
    @Transactional
    void getAllRentalsByCustomerIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where customerId not equals to DEFAULT_CUSTOMER_ID
        defaultRentalShouldNotBeFound("customerId.notEquals=" + DEFAULT_CUSTOMER_ID);

        // Get all the rentalList where customerId not equals to UPDATED_CUSTOMER_ID
        defaultRentalShouldBeFound("customerId.notEquals=" + UPDATED_CUSTOMER_ID);
    }

    @Test
    @Transactional
    void getAllRentalsByCustomerIdIsInShouldWork() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where customerId in DEFAULT_CUSTOMER_ID or UPDATED_CUSTOMER_ID
        defaultRentalShouldBeFound("customerId.in=" + DEFAULT_CUSTOMER_ID + "," + UPDATED_CUSTOMER_ID);

        // Get all the rentalList where customerId equals to UPDATED_CUSTOMER_ID
        defaultRentalShouldNotBeFound("customerId.in=" + UPDATED_CUSTOMER_ID);
    }

    @Test
    @Transactional
    void getAllRentalsByCustomerIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where customerId is not null
        defaultRentalShouldBeFound("customerId.specified=true");

        // Get all the rentalList where customerId is null
        defaultRentalShouldNotBeFound("customerId.specified=false");
    }

    @Test
    @Transactional
    void getAllRentalsByCustomerIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where customerId is greater than or equal to DEFAULT_CUSTOMER_ID
        defaultRentalShouldBeFound("customerId.greaterThanOrEqual=" + DEFAULT_CUSTOMER_ID);

        // Get all the rentalList where customerId is greater than or equal to UPDATED_CUSTOMER_ID
        defaultRentalShouldNotBeFound("customerId.greaterThanOrEqual=" + UPDATED_CUSTOMER_ID);
    }

    @Test
    @Transactional
    void getAllRentalsByCustomerIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where customerId is less than or equal to DEFAULT_CUSTOMER_ID
        defaultRentalShouldBeFound("customerId.lessThanOrEqual=" + DEFAULT_CUSTOMER_ID);

        // Get all the rentalList where customerId is less than or equal to SMALLER_CUSTOMER_ID
        defaultRentalShouldNotBeFound("customerId.lessThanOrEqual=" + SMALLER_CUSTOMER_ID);
    }

    @Test
    @Transactional
    void getAllRentalsByCustomerIdIsLessThanSomething() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where customerId is less than DEFAULT_CUSTOMER_ID
        defaultRentalShouldNotBeFound("customerId.lessThan=" + DEFAULT_CUSTOMER_ID);

        // Get all the rentalList where customerId is less than UPDATED_CUSTOMER_ID
        defaultRentalShouldBeFound("customerId.lessThan=" + UPDATED_CUSTOMER_ID);
    }

    @Test
    @Transactional
    void getAllRentalsByCustomerIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where customerId is greater than DEFAULT_CUSTOMER_ID
        defaultRentalShouldNotBeFound("customerId.greaterThan=" + DEFAULT_CUSTOMER_ID);

        // Get all the rentalList where customerId is greater than SMALLER_CUSTOMER_ID
        defaultRentalShouldBeFound("customerId.greaterThan=" + SMALLER_CUSTOMER_ID);
    }

    @Test
    @Transactional
    void getAllRentalsByCarIdIsEqualToSomething() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where carId equals to DEFAULT_CAR_ID
        defaultRentalShouldBeFound("carId.equals=" + DEFAULT_CAR_ID);

        // Get all the rentalList where carId equals to UPDATED_CAR_ID
        defaultRentalShouldNotBeFound("carId.equals=" + UPDATED_CAR_ID);
    }

    @Test
    @Transactional
    void getAllRentalsByCarIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where carId not equals to DEFAULT_CAR_ID
        defaultRentalShouldNotBeFound("carId.notEquals=" + DEFAULT_CAR_ID);

        // Get all the rentalList where carId not equals to UPDATED_CAR_ID
        defaultRentalShouldBeFound("carId.notEquals=" + UPDATED_CAR_ID);
    }

    @Test
    @Transactional
    void getAllRentalsByCarIdIsInShouldWork() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where carId in DEFAULT_CAR_ID or UPDATED_CAR_ID
        defaultRentalShouldBeFound("carId.in=" + DEFAULT_CAR_ID + "," + UPDATED_CAR_ID);

        // Get all the rentalList where carId equals to UPDATED_CAR_ID
        defaultRentalShouldNotBeFound("carId.in=" + UPDATED_CAR_ID);
    }

    @Test
    @Transactional
    void getAllRentalsByCarIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where carId is not null
        defaultRentalShouldBeFound("carId.specified=true");

        // Get all the rentalList where carId is null
        defaultRentalShouldNotBeFound("carId.specified=false");
    }

    @Test
    @Transactional
    void getAllRentalsByCarIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where carId is greater than or equal to DEFAULT_CAR_ID
        defaultRentalShouldBeFound("carId.greaterThanOrEqual=" + DEFAULT_CAR_ID);

        // Get all the rentalList where carId is greater than or equal to UPDATED_CAR_ID
        defaultRentalShouldNotBeFound("carId.greaterThanOrEqual=" + UPDATED_CAR_ID);
    }

    @Test
    @Transactional
    void getAllRentalsByCarIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where carId is less than or equal to DEFAULT_CAR_ID
        defaultRentalShouldBeFound("carId.lessThanOrEqual=" + DEFAULT_CAR_ID);

        // Get all the rentalList where carId is less than or equal to SMALLER_CAR_ID
        defaultRentalShouldNotBeFound("carId.lessThanOrEqual=" + SMALLER_CAR_ID);
    }

    @Test
    @Transactional
    void getAllRentalsByCarIdIsLessThanSomething() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where carId is less than DEFAULT_CAR_ID
        defaultRentalShouldNotBeFound("carId.lessThan=" + DEFAULT_CAR_ID);

        // Get all the rentalList where carId is less than UPDATED_CAR_ID
        defaultRentalShouldBeFound("carId.lessThan=" + UPDATED_CAR_ID);
    }

    @Test
    @Transactional
    void getAllRentalsByCarIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        // Get all the rentalList where carId is greater than DEFAULT_CAR_ID
        defaultRentalShouldNotBeFound("carId.greaterThan=" + DEFAULT_CAR_ID);

        // Get all the rentalList where carId is greater than SMALLER_CAR_ID
        defaultRentalShouldBeFound("carId.greaterThan=" + SMALLER_CAR_ID);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultRentalShouldBeFound(String filter) throws Exception {
        restRentalMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(rental.getId().intValue())))
            .andExpect(jsonPath("$.[*].rentDate").value(hasItem(sameInstant(DEFAULT_RENT_DATE))))
            .andExpect(jsonPath("$.[*].returnDate").value(hasItem(sameInstant(DEFAULT_RETURN_DATE))))
            .andExpect(jsonPath("$.[*].customerId").value(hasItem(DEFAULT_CUSTOMER_ID.intValue())))
            .andExpect(jsonPath("$.[*].carId").value(hasItem(DEFAULT_CAR_ID.intValue())));

        // Check, that the count call also returns 1
        restRentalMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultRentalShouldNotBeFound(String filter) throws Exception {
        restRentalMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restRentalMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingRental() throws Exception {
        // Get the rental
        restRentalMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewRental() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        int databaseSizeBeforeUpdate = rentalRepository.findAll().size();

        // Update the rental
        Rental updatedRental = rentalRepository.findById(rental.getId()).get();
        // Disconnect from session so that the updates on updatedRental are not directly saved in db
        em.detach(updatedRental);
        updatedRental.rentDate(UPDATED_RENT_DATE).returnDate(UPDATED_RETURN_DATE).customerId(UPDATED_CUSTOMER_ID).carId(UPDATED_CAR_ID);
        RentalDTO rentalDTO = rentalMapper.toDto(updatedRental);

        restRentalMockMvc
            .perform(
                put(ENTITY_API_URL_ID, rentalDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(rentalDTO))
            )
            .andExpect(status().isOk());

        // Validate the Rental in the database
        List<Rental> rentalList = rentalRepository.findAll();
        assertThat(rentalList).hasSize(databaseSizeBeforeUpdate);
        Rental testRental = rentalList.get(rentalList.size() - 1);
        assertThat(testRental.getRentDate()).isEqualTo(UPDATED_RENT_DATE);
        assertThat(testRental.getReturnDate()).isEqualTo(UPDATED_RETURN_DATE);
        assertThat(testRental.getCustomerId()).isEqualTo(UPDATED_CUSTOMER_ID);
        assertThat(testRental.getCarId()).isEqualTo(UPDATED_CAR_ID);

        // Validate the Rental in Elasticsearch
        verify(mockRentalSearchRepository).save(testRental);
    }

    @Test
    @Transactional
    void putNonExistingRental() throws Exception {
        int databaseSizeBeforeUpdate = rentalRepository.findAll().size();
        rental.setId(count.incrementAndGet());

        // Create the Rental
        RentalDTO rentalDTO = rentalMapper.toDto(rental);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRentalMockMvc
            .perform(
                put(ENTITY_API_URL_ID, rentalDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(rentalDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Rental in the database
        List<Rental> rentalList = rentalRepository.findAll();
        assertThat(rentalList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Rental in Elasticsearch
        verify(mockRentalSearchRepository, times(0)).save(rental);
    }

    @Test
    @Transactional
    void putWithIdMismatchRental() throws Exception {
        int databaseSizeBeforeUpdate = rentalRepository.findAll().size();
        rental.setId(count.incrementAndGet());

        // Create the Rental
        RentalDTO rentalDTO = rentalMapper.toDto(rental);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRentalMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(rentalDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Rental in the database
        List<Rental> rentalList = rentalRepository.findAll();
        assertThat(rentalList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Rental in Elasticsearch
        verify(mockRentalSearchRepository, times(0)).save(rental);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamRental() throws Exception {
        int databaseSizeBeforeUpdate = rentalRepository.findAll().size();
        rental.setId(count.incrementAndGet());

        // Create the Rental
        RentalDTO rentalDTO = rentalMapper.toDto(rental);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRentalMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(rentalDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Rental in the database
        List<Rental> rentalList = rentalRepository.findAll();
        assertThat(rentalList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Rental in Elasticsearch
        verify(mockRentalSearchRepository, times(0)).save(rental);
    }

    @Test
    @Transactional
    void partialUpdateRentalWithPatch() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        int databaseSizeBeforeUpdate = rentalRepository.findAll().size();

        // Update the rental using partial update
        Rental partialUpdatedRental = new Rental();
        partialUpdatedRental.setId(rental.getId());

        partialUpdatedRental.customerId(UPDATED_CUSTOMER_ID).carId(UPDATED_CAR_ID);

        restRentalMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRental.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedRental))
            )
            .andExpect(status().isOk());

        // Validate the Rental in the database
        List<Rental> rentalList = rentalRepository.findAll();
        assertThat(rentalList).hasSize(databaseSizeBeforeUpdate);
        Rental testRental = rentalList.get(rentalList.size() - 1);
        assertThat(testRental.getRentDate()).isEqualTo(DEFAULT_RENT_DATE);
        assertThat(testRental.getReturnDate()).isEqualTo(DEFAULT_RETURN_DATE);
        assertThat(testRental.getCustomerId()).isEqualTo(UPDATED_CUSTOMER_ID);
        assertThat(testRental.getCarId()).isEqualTo(UPDATED_CAR_ID);
    }

    @Test
    @Transactional
    void fullUpdateRentalWithPatch() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        int databaseSizeBeforeUpdate = rentalRepository.findAll().size();

        // Update the rental using partial update
        Rental partialUpdatedRental = new Rental();
        partialUpdatedRental.setId(rental.getId());

        partialUpdatedRental
            .rentDate(UPDATED_RENT_DATE)
            .returnDate(UPDATED_RETURN_DATE)
            .customerId(UPDATED_CUSTOMER_ID)
            .carId(UPDATED_CAR_ID);

        restRentalMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRental.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedRental))
            )
            .andExpect(status().isOk());

        // Validate the Rental in the database
        List<Rental> rentalList = rentalRepository.findAll();
        assertThat(rentalList).hasSize(databaseSizeBeforeUpdate);
        Rental testRental = rentalList.get(rentalList.size() - 1);
        assertThat(testRental.getRentDate()).isEqualTo(UPDATED_RENT_DATE);
        assertThat(testRental.getReturnDate()).isEqualTo(UPDATED_RETURN_DATE);
        assertThat(testRental.getCustomerId()).isEqualTo(UPDATED_CUSTOMER_ID);
        assertThat(testRental.getCarId()).isEqualTo(UPDATED_CAR_ID);
    }

    @Test
    @Transactional
    void patchNonExistingRental() throws Exception {
        int databaseSizeBeforeUpdate = rentalRepository.findAll().size();
        rental.setId(count.incrementAndGet());

        // Create the Rental
        RentalDTO rentalDTO = rentalMapper.toDto(rental);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRentalMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, rentalDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(rentalDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Rental in the database
        List<Rental> rentalList = rentalRepository.findAll();
        assertThat(rentalList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Rental in Elasticsearch
        verify(mockRentalSearchRepository, times(0)).save(rental);
    }

    @Test
    @Transactional
    void patchWithIdMismatchRental() throws Exception {
        int databaseSizeBeforeUpdate = rentalRepository.findAll().size();
        rental.setId(count.incrementAndGet());

        // Create the Rental
        RentalDTO rentalDTO = rentalMapper.toDto(rental);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRentalMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(rentalDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Rental in the database
        List<Rental> rentalList = rentalRepository.findAll();
        assertThat(rentalList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Rental in Elasticsearch
        verify(mockRentalSearchRepository, times(0)).save(rental);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamRental() throws Exception {
        int databaseSizeBeforeUpdate = rentalRepository.findAll().size();
        rental.setId(count.incrementAndGet());

        // Create the Rental
        RentalDTO rentalDTO = rentalMapper.toDto(rental);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRentalMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(rentalDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Rental in the database
        List<Rental> rentalList = rentalRepository.findAll();
        assertThat(rentalList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Rental in Elasticsearch
        verify(mockRentalSearchRepository, times(0)).save(rental);
    }

    @Test
    @Transactional
    void deleteRental() throws Exception {
        // Initialize the database
        rentalRepository.saveAndFlush(rental);

        int databaseSizeBeforeDelete = rentalRepository.findAll().size();

        // Delete the rental
        restRentalMockMvc
            .perform(delete(ENTITY_API_URL_ID, rental.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Rental> rentalList = rentalRepository.findAll();
        assertThat(rentalList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Rental in Elasticsearch
        verify(mockRentalSearchRepository, times(1)).deleteById(rental.getId());
    }

    @Test
    @Transactional
    void searchRental() throws Exception {
        // Configure the mock search repository
        // Initialize the database
        rentalRepository.saveAndFlush(rental);
        when(mockRentalSearchRepository.search(queryStringQuery("id:" + rental.getId()), PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(Collections.singletonList(rental), PageRequest.of(0, 1), 1));

        // Search the rental
        restRentalMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + rental.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(rental.getId().intValue())))
            .andExpect(jsonPath("$.[*].rentDate").value(hasItem(sameInstant(DEFAULT_RENT_DATE))))
            .andExpect(jsonPath("$.[*].returnDate").value(hasItem(sameInstant(DEFAULT_RETURN_DATE))))
            .andExpect(jsonPath("$.[*].customerId").value(hasItem(DEFAULT_CUSTOMER_ID.intValue())))
            .andExpect(jsonPath("$.[*].carId").value(hasItem(DEFAULT_CAR_ID.intValue())));
    }
}
