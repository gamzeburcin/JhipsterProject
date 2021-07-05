package com.rentacar.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.rentacar.IntegrationTest;
import com.rentacar.domain.Car;
import com.rentacar.repository.CarRepository;
import com.rentacar.repository.search.CarSearchRepository;
import com.rentacar.service.criteria.CarCriteria;
import com.rentacar.service.dto.CarDTO;
import com.rentacar.service.mapper.CarMapper;
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
 * Integration tests for the {@link CarResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class CarResourceIT {

    private static final Long DEFAULT_BRAND_ID = 1L;
    private static final Long UPDATED_BRAND_ID = 2L;
    private static final Long SMALLER_BRAND_ID = 1L - 1L;

    private static final Long DEFAULT_COLOR_ID = 1L;
    private static final Long UPDATED_COLOR_ID = 2L;
    private static final Long SMALLER_COLOR_ID = 1L - 1L;

    private static final String DEFAULT_MODEL_YEAR = "AAAAAAAAAA";
    private static final String UPDATED_MODEL_YEAR = "BBBBBBBBBB";

    private static final Double DEFAULT_DAILY_PRICE = 1D;
    private static final Double UPDATED_DAILY_PRICE = 2D;
    private static final Double SMALLER_DAILY_PRICE = 1D - 1D;

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/cars";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/cars";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CarMapper carMapper;

    /**
     * This repository is mocked in the com.rentacar.repository.search test package.
     *
     * @see com.rentacar.repository.search.CarSearchRepositoryMockConfiguration
     */
    @Autowired
    private CarSearchRepository mockCarSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCarMockMvc;

    private Car car;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Car createEntity(EntityManager em) {
        Car car = new Car()
            .brandId(DEFAULT_BRAND_ID)
            .colorId(DEFAULT_COLOR_ID)
            .modelYear(DEFAULT_MODEL_YEAR)
            .dailyPrice(DEFAULT_DAILY_PRICE)
            .description(DEFAULT_DESCRIPTION);
        return car;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Car createUpdatedEntity(EntityManager em) {
        Car car = new Car()
            .brandId(UPDATED_BRAND_ID)
            .colorId(UPDATED_COLOR_ID)
            .modelYear(UPDATED_MODEL_YEAR)
            .dailyPrice(UPDATED_DAILY_PRICE)
            .description(UPDATED_DESCRIPTION);
        return car;
    }

    @BeforeEach
    public void initTest() {
        car = createEntity(em);
    }

    @Test
    @Transactional
    void createCar() throws Exception {
        int databaseSizeBeforeCreate = carRepository.findAll().size();
        // Create the Car
        CarDTO carDTO = carMapper.toDto(car);
        restCarMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(carDTO)))
            .andExpect(status().isCreated());

        // Validate the Car in the database
        List<Car> carList = carRepository.findAll();
        assertThat(carList).hasSize(databaseSizeBeforeCreate + 1);
        Car testCar = carList.get(carList.size() - 1);
        assertThat(testCar.getBrandId()).isEqualTo(DEFAULT_BRAND_ID);
        assertThat(testCar.getColorId()).isEqualTo(DEFAULT_COLOR_ID);
        assertThat(testCar.getModelYear()).isEqualTo(DEFAULT_MODEL_YEAR);
        assertThat(testCar.getDailyPrice()).isEqualTo(DEFAULT_DAILY_PRICE);
        assertThat(testCar.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);

        // Validate the Car in Elasticsearch
        verify(mockCarSearchRepository, times(1)).save(testCar);
    }

    @Test
    @Transactional
    void createCarWithExistingId() throws Exception {
        // Create the Car with an existing ID
        car.setId(1L);
        CarDTO carDTO = carMapper.toDto(car);

        int databaseSizeBeforeCreate = carRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCarMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(carDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Car in the database
        List<Car> carList = carRepository.findAll();
        assertThat(carList).hasSize(databaseSizeBeforeCreate);

        // Validate the Car in Elasticsearch
        verify(mockCarSearchRepository, times(0)).save(car);
    }

    @Test
    @Transactional
    void getAllCars() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList
        restCarMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(car.getId().intValue())))
            .andExpect(jsonPath("$.[*].brandId").value(hasItem(DEFAULT_BRAND_ID.intValue())))
            .andExpect(jsonPath("$.[*].colorId").value(hasItem(DEFAULT_COLOR_ID.intValue())))
            .andExpect(jsonPath("$.[*].modelYear").value(hasItem(DEFAULT_MODEL_YEAR)))
            .andExpect(jsonPath("$.[*].dailyPrice").value(hasItem(DEFAULT_DAILY_PRICE.doubleValue())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));
    }

    @Test
    @Transactional
    void getCar() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get the car
        restCarMockMvc
            .perform(get(ENTITY_API_URL_ID, car.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(car.getId().intValue()))
            .andExpect(jsonPath("$.brandId").value(DEFAULT_BRAND_ID.intValue()))
            .andExpect(jsonPath("$.colorId").value(DEFAULT_COLOR_ID.intValue()))
            .andExpect(jsonPath("$.modelYear").value(DEFAULT_MODEL_YEAR))
            .andExpect(jsonPath("$.dailyPrice").value(DEFAULT_DAILY_PRICE.doubleValue()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION));
    }

    @Test
    @Transactional
    void getCarsByIdFiltering() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        Long id = car.getId();

        defaultCarShouldBeFound("id.equals=" + id);
        defaultCarShouldNotBeFound("id.notEquals=" + id);

        defaultCarShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultCarShouldNotBeFound("id.greaterThan=" + id);

        defaultCarShouldBeFound("id.lessThanOrEqual=" + id);
        defaultCarShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllCarsByBrandIdIsEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where brandId equals to DEFAULT_BRAND_ID
        defaultCarShouldBeFound("brandId.equals=" + DEFAULT_BRAND_ID);

        // Get all the carList where brandId equals to UPDATED_BRAND_ID
        defaultCarShouldNotBeFound("brandId.equals=" + UPDATED_BRAND_ID);
    }

    @Test
    @Transactional
    void getAllCarsByBrandIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where brandId not equals to DEFAULT_BRAND_ID
        defaultCarShouldNotBeFound("brandId.notEquals=" + DEFAULT_BRAND_ID);

        // Get all the carList where brandId not equals to UPDATED_BRAND_ID
        defaultCarShouldBeFound("brandId.notEquals=" + UPDATED_BRAND_ID);
    }

    @Test
    @Transactional
    void getAllCarsByBrandIdIsInShouldWork() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where brandId in DEFAULT_BRAND_ID or UPDATED_BRAND_ID
        defaultCarShouldBeFound("brandId.in=" + DEFAULT_BRAND_ID + "," + UPDATED_BRAND_ID);

        // Get all the carList where brandId equals to UPDATED_BRAND_ID
        defaultCarShouldNotBeFound("brandId.in=" + UPDATED_BRAND_ID);
    }

    @Test
    @Transactional
    void getAllCarsByBrandIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where brandId is not null
        defaultCarShouldBeFound("brandId.specified=true");

        // Get all the carList where brandId is null
        defaultCarShouldNotBeFound("brandId.specified=false");
    }

    @Test
    @Transactional
    void getAllCarsByBrandIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where brandId is greater than or equal to DEFAULT_BRAND_ID
        defaultCarShouldBeFound("brandId.greaterThanOrEqual=" + DEFAULT_BRAND_ID);

        // Get all the carList where brandId is greater than or equal to UPDATED_BRAND_ID
        defaultCarShouldNotBeFound("brandId.greaterThanOrEqual=" + UPDATED_BRAND_ID);
    }

    @Test
    @Transactional
    void getAllCarsByBrandIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where brandId is less than or equal to DEFAULT_BRAND_ID
        defaultCarShouldBeFound("brandId.lessThanOrEqual=" + DEFAULT_BRAND_ID);

        // Get all the carList where brandId is less than or equal to SMALLER_BRAND_ID
        defaultCarShouldNotBeFound("brandId.lessThanOrEqual=" + SMALLER_BRAND_ID);
    }

    @Test
    @Transactional
    void getAllCarsByBrandIdIsLessThanSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where brandId is less than DEFAULT_BRAND_ID
        defaultCarShouldNotBeFound("brandId.lessThan=" + DEFAULT_BRAND_ID);

        // Get all the carList where brandId is less than UPDATED_BRAND_ID
        defaultCarShouldBeFound("brandId.lessThan=" + UPDATED_BRAND_ID);
    }

    @Test
    @Transactional
    void getAllCarsByBrandIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where brandId is greater than DEFAULT_BRAND_ID
        defaultCarShouldNotBeFound("brandId.greaterThan=" + DEFAULT_BRAND_ID);

        // Get all the carList where brandId is greater than SMALLER_BRAND_ID
        defaultCarShouldBeFound("brandId.greaterThan=" + SMALLER_BRAND_ID);
    }

    @Test
    @Transactional
    void getAllCarsByColorIdIsEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where colorId equals to DEFAULT_COLOR_ID
        defaultCarShouldBeFound("colorId.equals=" + DEFAULT_COLOR_ID);

        // Get all the carList where colorId equals to UPDATED_COLOR_ID
        defaultCarShouldNotBeFound("colorId.equals=" + UPDATED_COLOR_ID);
    }

    @Test
    @Transactional
    void getAllCarsByColorIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where colorId not equals to DEFAULT_COLOR_ID
        defaultCarShouldNotBeFound("colorId.notEquals=" + DEFAULT_COLOR_ID);

        // Get all the carList where colorId not equals to UPDATED_COLOR_ID
        defaultCarShouldBeFound("colorId.notEquals=" + UPDATED_COLOR_ID);
    }

    @Test
    @Transactional
    void getAllCarsByColorIdIsInShouldWork() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where colorId in DEFAULT_COLOR_ID or UPDATED_COLOR_ID
        defaultCarShouldBeFound("colorId.in=" + DEFAULT_COLOR_ID + "," + UPDATED_COLOR_ID);

        // Get all the carList where colorId equals to UPDATED_COLOR_ID
        defaultCarShouldNotBeFound("colorId.in=" + UPDATED_COLOR_ID);
    }

    @Test
    @Transactional
    void getAllCarsByColorIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where colorId is not null
        defaultCarShouldBeFound("colorId.specified=true");

        // Get all the carList where colorId is null
        defaultCarShouldNotBeFound("colorId.specified=false");
    }

    @Test
    @Transactional
    void getAllCarsByColorIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where colorId is greater than or equal to DEFAULT_COLOR_ID
        defaultCarShouldBeFound("colorId.greaterThanOrEqual=" + DEFAULT_COLOR_ID);

        // Get all the carList where colorId is greater than or equal to UPDATED_COLOR_ID
        defaultCarShouldNotBeFound("colorId.greaterThanOrEqual=" + UPDATED_COLOR_ID);
    }

    @Test
    @Transactional
    void getAllCarsByColorIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where colorId is less than or equal to DEFAULT_COLOR_ID
        defaultCarShouldBeFound("colorId.lessThanOrEqual=" + DEFAULT_COLOR_ID);

        // Get all the carList where colorId is less than or equal to SMALLER_COLOR_ID
        defaultCarShouldNotBeFound("colorId.lessThanOrEqual=" + SMALLER_COLOR_ID);
    }

    @Test
    @Transactional
    void getAllCarsByColorIdIsLessThanSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where colorId is less than DEFAULT_COLOR_ID
        defaultCarShouldNotBeFound("colorId.lessThan=" + DEFAULT_COLOR_ID);

        // Get all the carList where colorId is less than UPDATED_COLOR_ID
        defaultCarShouldBeFound("colorId.lessThan=" + UPDATED_COLOR_ID);
    }

    @Test
    @Transactional
    void getAllCarsByColorIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where colorId is greater than DEFAULT_COLOR_ID
        defaultCarShouldNotBeFound("colorId.greaterThan=" + DEFAULT_COLOR_ID);

        // Get all the carList where colorId is greater than SMALLER_COLOR_ID
        defaultCarShouldBeFound("colorId.greaterThan=" + SMALLER_COLOR_ID);
    }

    @Test
    @Transactional
    void getAllCarsByModelYearIsEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where modelYear equals to DEFAULT_MODEL_YEAR
        defaultCarShouldBeFound("modelYear.equals=" + DEFAULT_MODEL_YEAR);

        // Get all the carList where modelYear equals to UPDATED_MODEL_YEAR
        defaultCarShouldNotBeFound("modelYear.equals=" + UPDATED_MODEL_YEAR);
    }

    @Test
    @Transactional
    void getAllCarsByModelYearIsNotEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where modelYear not equals to DEFAULT_MODEL_YEAR
        defaultCarShouldNotBeFound("modelYear.notEquals=" + DEFAULT_MODEL_YEAR);

        // Get all the carList where modelYear not equals to UPDATED_MODEL_YEAR
        defaultCarShouldBeFound("modelYear.notEquals=" + UPDATED_MODEL_YEAR);
    }

    @Test
    @Transactional
    void getAllCarsByModelYearIsInShouldWork() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where modelYear in DEFAULT_MODEL_YEAR or UPDATED_MODEL_YEAR
        defaultCarShouldBeFound("modelYear.in=" + DEFAULT_MODEL_YEAR + "," + UPDATED_MODEL_YEAR);

        // Get all the carList where modelYear equals to UPDATED_MODEL_YEAR
        defaultCarShouldNotBeFound("modelYear.in=" + UPDATED_MODEL_YEAR);
    }

    @Test
    @Transactional
    void getAllCarsByModelYearIsNullOrNotNull() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where modelYear is not null
        defaultCarShouldBeFound("modelYear.specified=true");

        // Get all the carList where modelYear is null
        defaultCarShouldNotBeFound("modelYear.specified=false");
    }

    @Test
    @Transactional
    void getAllCarsByModelYearContainsSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where modelYear contains DEFAULT_MODEL_YEAR
        defaultCarShouldBeFound("modelYear.contains=" + DEFAULT_MODEL_YEAR);

        // Get all the carList where modelYear contains UPDATED_MODEL_YEAR
        defaultCarShouldNotBeFound("modelYear.contains=" + UPDATED_MODEL_YEAR);
    }

    @Test
    @Transactional
    void getAllCarsByModelYearNotContainsSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where modelYear does not contain DEFAULT_MODEL_YEAR
        defaultCarShouldNotBeFound("modelYear.doesNotContain=" + DEFAULT_MODEL_YEAR);

        // Get all the carList where modelYear does not contain UPDATED_MODEL_YEAR
        defaultCarShouldBeFound("modelYear.doesNotContain=" + UPDATED_MODEL_YEAR);
    }

    @Test
    @Transactional
    void getAllCarsByDailyPriceIsEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where dailyPrice equals to DEFAULT_DAILY_PRICE
        defaultCarShouldBeFound("dailyPrice.equals=" + DEFAULT_DAILY_PRICE);

        // Get all the carList where dailyPrice equals to UPDATED_DAILY_PRICE
        defaultCarShouldNotBeFound("dailyPrice.equals=" + UPDATED_DAILY_PRICE);
    }

    @Test
    @Transactional
    void getAllCarsByDailyPriceIsNotEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where dailyPrice not equals to DEFAULT_DAILY_PRICE
        defaultCarShouldNotBeFound("dailyPrice.notEquals=" + DEFAULT_DAILY_PRICE);

        // Get all the carList where dailyPrice not equals to UPDATED_DAILY_PRICE
        defaultCarShouldBeFound("dailyPrice.notEquals=" + UPDATED_DAILY_PRICE);
    }

    @Test
    @Transactional
    void getAllCarsByDailyPriceIsInShouldWork() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where dailyPrice in DEFAULT_DAILY_PRICE or UPDATED_DAILY_PRICE
        defaultCarShouldBeFound("dailyPrice.in=" + DEFAULT_DAILY_PRICE + "," + UPDATED_DAILY_PRICE);

        // Get all the carList where dailyPrice equals to UPDATED_DAILY_PRICE
        defaultCarShouldNotBeFound("dailyPrice.in=" + UPDATED_DAILY_PRICE);
    }

    @Test
    @Transactional
    void getAllCarsByDailyPriceIsNullOrNotNull() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where dailyPrice is not null
        defaultCarShouldBeFound("dailyPrice.specified=true");

        // Get all the carList where dailyPrice is null
        defaultCarShouldNotBeFound("dailyPrice.specified=false");
    }

    @Test
    @Transactional
    void getAllCarsByDailyPriceIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where dailyPrice is greater than or equal to DEFAULT_DAILY_PRICE
        defaultCarShouldBeFound("dailyPrice.greaterThanOrEqual=" + DEFAULT_DAILY_PRICE);

        // Get all the carList where dailyPrice is greater than or equal to UPDATED_DAILY_PRICE
        defaultCarShouldNotBeFound("dailyPrice.greaterThanOrEqual=" + UPDATED_DAILY_PRICE);
    }

    @Test
    @Transactional
    void getAllCarsByDailyPriceIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where dailyPrice is less than or equal to DEFAULT_DAILY_PRICE
        defaultCarShouldBeFound("dailyPrice.lessThanOrEqual=" + DEFAULT_DAILY_PRICE);

        // Get all the carList where dailyPrice is less than or equal to SMALLER_DAILY_PRICE
        defaultCarShouldNotBeFound("dailyPrice.lessThanOrEqual=" + SMALLER_DAILY_PRICE);
    }

    @Test
    @Transactional
    void getAllCarsByDailyPriceIsLessThanSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where dailyPrice is less than DEFAULT_DAILY_PRICE
        defaultCarShouldNotBeFound("dailyPrice.lessThan=" + DEFAULT_DAILY_PRICE);

        // Get all the carList where dailyPrice is less than UPDATED_DAILY_PRICE
        defaultCarShouldBeFound("dailyPrice.lessThan=" + UPDATED_DAILY_PRICE);
    }

    @Test
    @Transactional
    void getAllCarsByDailyPriceIsGreaterThanSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where dailyPrice is greater than DEFAULT_DAILY_PRICE
        defaultCarShouldNotBeFound("dailyPrice.greaterThan=" + DEFAULT_DAILY_PRICE);

        // Get all the carList where dailyPrice is greater than SMALLER_DAILY_PRICE
        defaultCarShouldBeFound("dailyPrice.greaterThan=" + SMALLER_DAILY_PRICE);
    }

    @Test
    @Transactional
    void getAllCarsByDescriptionIsEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where description equals to DEFAULT_DESCRIPTION
        defaultCarShouldBeFound("description.equals=" + DEFAULT_DESCRIPTION);

        // Get all the carList where description equals to UPDATED_DESCRIPTION
        defaultCarShouldNotBeFound("description.equals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllCarsByDescriptionIsNotEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where description not equals to DEFAULT_DESCRIPTION
        defaultCarShouldNotBeFound("description.notEquals=" + DEFAULT_DESCRIPTION);

        // Get all the carList where description not equals to UPDATED_DESCRIPTION
        defaultCarShouldBeFound("description.notEquals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllCarsByDescriptionIsInShouldWork() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where description in DEFAULT_DESCRIPTION or UPDATED_DESCRIPTION
        defaultCarShouldBeFound("description.in=" + DEFAULT_DESCRIPTION + "," + UPDATED_DESCRIPTION);

        // Get all the carList where description equals to UPDATED_DESCRIPTION
        defaultCarShouldNotBeFound("description.in=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllCarsByDescriptionIsNullOrNotNull() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where description is not null
        defaultCarShouldBeFound("description.specified=true");

        // Get all the carList where description is null
        defaultCarShouldNotBeFound("description.specified=false");
    }

    @Test
    @Transactional
    void getAllCarsByDescriptionContainsSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where description contains DEFAULT_DESCRIPTION
        defaultCarShouldBeFound("description.contains=" + DEFAULT_DESCRIPTION);

        // Get all the carList where description contains UPDATED_DESCRIPTION
        defaultCarShouldNotBeFound("description.contains=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllCarsByDescriptionNotContainsSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where description does not contain DEFAULT_DESCRIPTION
        defaultCarShouldNotBeFound("description.doesNotContain=" + DEFAULT_DESCRIPTION);

        // Get all the carList where description does not contain UPDATED_DESCRIPTION
        defaultCarShouldBeFound("description.doesNotContain=" + UPDATED_DESCRIPTION);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCarShouldBeFound(String filter) throws Exception {
        restCarMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(car.getId().intValue())))
            .andExpect(jsonPath("$.[*].brandId").value(hasItem(DEFAULT_BRAND_ID.intValue())))
            .andExpect(jsonPath("$.[*].colorId").value(hasItem(DEFAULT_COLOR_ID.intValue())))
            .andExpect(jsonPath("$.[*].modelYear").value(hasItem(DEFAULT_MODEL_YEAR)))
            .andExpect(jsonPath("$.[*].dailyPrice").value(hasItem(DEFAULT_DAILY_PRICE.doubleValue())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));

        // Check, that the count call also returns 1
        restCarMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCarShouldNotBeFound(String filter) throws Exception {
        restCarMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCarMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingCar() throws Exception {
        // Get the car
        restCarMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewCar() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        int databaseSizeBeforeUpdate = carRepository.findAll().size();

        // Update the car
        Car updatedCar = carRepository.findById(car.getId()).get();
        // Disconnect from session so that the updates on updatedCar are not directly saved in db
        em.detach(updatedCar);
        updatedCar
            .brandId(UPDATED_BRAND_ID)
            .colorId(UPDATED_COLOR_ID)
            .modelYear(UPDATED_MODEL_YEAR)
            .dailyPrice(UPDATED_DAILY_PRICE)
            .description(UPDATED_DESCRIPTION);
        CarDTO carDTO = carMapper.toDto(updatedCar);

        restCarMockMvc
            .perform(
                put(ENTITY_API_URL_ID, carDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(carDTO))
            )
            .andExpect(status().isOk());

        // Validate the Car in the database
        List<Car> carList = carRepository.findAll();
        assertThat(carList).hasSize(databaseSizeBeforeUpdate);
        Car testCar = carList.get(carList.size() - 1);
        assertThat(testCar.getBrandId()).isEqualTo(UPDATED_BRAND_ID);
        assertThat(testCar.getColorId()).isEqualTo(UPDATED_COLOR_ID);
        assertThat(testCar.getModelYear()).isEqualTo(UPDATED_MODEL_YEAR);
        assertThat(testCar.getDailyPrice()).isEqualTo(UPDATED_DAILY_PRICE);
        assertThat(testCar.getDescription()).isEqualTo(UPDATED_DESCRIPTION);

        // Validate the Car in Elasticsearch
        verify(mockCarSearchRepository).save(testCar);
    }

    @Test
    @Transactional
    void putNonExistingCar() throws Exception {
        int databaseSizeBeforeUpdate = carRepository.findAll().size();
        car.setId(count.incrementAndGet());

        // Create the Car
        CarDTO carDTO = carMapper.toDto(car);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCarMockMvc
            .perform(
                put(ENTITY_API_URL_ID, carDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(carDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Car in the database
        List<Car> carList = carRepository.findAll();
        assertThat(carList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Car in Elasticsearch
        verify(mockCarSearchRepository, times(0)).save(car);
    }

    @Test
    @Transactional
    void putWithIdMismatchCar() throws Exception {
        int databaseSizeBeforeUpdate = carRepository.findAll().size();
        car.setId(count.incrementAndGet());

        // Create the Car
        CarDTO carDTO = carMapper.toDto(car);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCarMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(carDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Car in the database
        List<Car> carList = carRepository.findAll();
        assertThat(carList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Car in Elasticsearch
        verify(mockCarSearchRepository, times(0)).save(car);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCar() throws Exception {
        int databaseSizeBeforeUpdate = carRepository.findAll().size();
        car.setId(count.incrementAndGet());

        // Create the Car
        CarDTO carDTO = carMapper.toDto(car);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCarMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(carDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Car in the database
        List<Car> carList = carRepository.findAll();
        assertThat(carList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Car in Elasticsearch
        verify(mockCarSearchRepository, times(0)).save(car);
    }

    @Test
    @Transactional
    void partialUpdateCarWithPatch() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        int databaseSizeBeforeUpdate = carRepository.findAll().size();

        // Update the car using partial update
        Car partialUpdatedCar = new Car();
        partialUpdatedCar.setId(car.getId());

        partialUpdatedCar
            .brandId(UPDATED_BRAND_ID)
            .colorId(UPDATED_COLOR_ID)
            .modelYear(UPDATED_MODEL_YEAR)
            .description(UPDATED_DESCRIPTION);

        restCarMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCar.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCar))
            )
            .andExpect(status().isOk());

        // Validate the Car in the database
        List<Car> carList = carRepository.findAll();
        assertThat(carList).hasSize(databaseSizeBeforeUpdate);
        Car testCar = carList.get(carList.size() - 1);
        assertThat(testCar.getBrandId()).isEqualTo(UPDATED_BRAND_ID);
        assertThat(testCar.getColorId()).isEqualTo(UPDATED_COLOR_ID);
        assertThat(testCar.getModelYear()).isEqualTo(UPDATED_MODEL_YEAR);
        assertThat(testCar.getDailyPrice()).isEqualTo(DEFAULT_DAILY_PRICE);
        assertThat(testCar.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void fullUpdateCarWithPatch() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        int databaseSizeBeforeUpdate = carRepository.findAll().size();

        // Update the car using partial update
        Car partialUpdatedCar = new Car();
        partialUpdatedCar.setId(car.getId());

        partialUpdatedCar
            .brandId(UPDATED_BRAND_ID)
            .colorId(UPDATED_COLOR_ID)
            .modelYear(UPDATED_MODEL_YEAR)
            .dailyPrice(UPDATED_DAILY_PRICE)
            .description(UPDATED_DESCRIPTION);

        restCarMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCar.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCar))
            )
            .andExpect(status().isOk());

        // Validate the Car in the database
        List<Car> carList = carRepository.findAll();
        assertThat(carList).hasSize(databaseSizeBeforeUpdate);
        Car testCar = carList.get(carList.size() - 1);
        assertThat(testCar.getBrandId()).isEqualTo(UPDATED_BRAND_ID);
        assertThat(testCar.getColorId()).isEqualTo(UPDATED_COLOR_ID);
        assertThat(testCar.getModelYear()).isEqualTo(UPDATED_MODEL_YEAR);
        assertThat(testCar.getDailyPrice()).isEqualTo(UPDATED_DAILY_PRICE);
        assertThat(testCar.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void patchNonExistingCar() throws Exception {
        int databaseSizeBeforeUpdate = carRepository.findAll().size();
        car.setId(count.incrementAndGet());

        // Create the Car
        CarDTO carDTO = carMapper.toDto(car);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCarMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, carDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(carDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Car in the database
        List<Car> carList = carRepository.findAll();
        assertThat(carList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Car in Elasticsearch
        verify(mockCarSearchRepository, times(0)).save(car);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCar() throws Exception {
        int databaseSizeBeforeUpdate = carRepository.findAll().size();
        car.setId(count.incrementAndGet());

        // Create the Car
        CarDTO carDTO = carMapper.toDto(car);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCarMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(carDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Car in the database
        List<Car> carList = carRepository.findAll();
        assertThat(carList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Car in Elasticsearch
        verify(mockCarSearchRepository, times(0)).save(car);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCar() throws Exception {
        int databaseSizeBeforeUpdate = carRepository.findAll().size();
        car.setId(count.incrementAndGet());

        // Create the Car
        CarDTO carDTO = carMapper.toDto(car);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCarMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(carDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Car in the database
        List<Car> carList = carRepository.findAll();
        assertThat(carList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Car in Elasticsearch
        verify(mockCarSearchRepository, times(0)).save(car);
    }

    @Test
    @Transactional
    void deleteCar() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        int databaseSizeBeforeDelete = carRepository.findAll().size();

        // Delete the car
        restCarMockMvc.perform(delete(ENTITY_API_URL_ID, car.getId()).accept(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Car> carList = carRepository.findAll();
        assertThat(carList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Car in Elasticsearch
        verify(mockCarSearchRepository, times(1)).deleteById(car.getId());
    }

    @Test
    @Transactional
    void searchCar() throws Exception {
        // Configure the mock search repository
        // Initialize the database
        carRepository.saveAndFlush(car);
        when(mockCarSearchRepository.search(queryStringQuery("id:" + car.getId()), PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(Collections.singletonList(car), PageRequest.of(0, 1), 1));

        // Search the car
        restCarMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + car.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(car.getId().intValue())))
            .andExpect(jsonPath("$.[*].brandId").value(hasItem(DEFAULT_BRAND_ID.intValue())))
            .andExpect(jsonPath("$.[*].colorId").value(hasItem(DEFAULT_COLOR_ID.intValue())))
            .andExpect(jsonPath("$.[*].modelYear").value(hasItem(DEFAULT_MODEL_YEAR)))
            .andExpect(jsonPath("$.[*].dailyPrice").value(hasItem(DEFAULT_DAILY_PRICE.doubleValue())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));
    }
}
