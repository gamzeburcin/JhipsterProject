package com.rentacar.web.rest;

import static com.rentacar.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.rentacar.IntegrationTest;
import com.rentacar.domain.CarImage;
import com.rentacar.repository.CarImageRepository;
import com.rentacar.repository.search.CarImageSearchRepository;
import com.rentacar.service.criteria.CarImageCriteria;
import com.rentacar.service.dto.CarImageDTO;
import com.rentacar.service.mapper.CarImageMapper;
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
 * Integration tests for the {@link CarImageResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class CarImageResourceIT {

    private static final Long DEFAULT_CAR_ID = 1L;
    private static final Long UPDATED_CAR_ID = 2L;
    private static final Long SMALLER_CAR_ID = 1L - 1L;

    private static final String DEFAULT_IMAGE_PATH = "AAAAAAAAAA";
    private static final String UPDATED_IMAGE_PATH = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_DATE = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final ZonedDateTime SMALLER_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(-1L), ZoneOffset.UTC);

    private static final String ENTITY_API_URL = "/api/car-images";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/car-images";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private CarImageRepository carImageRepository;

    @Autowired
    private CarImageMapper carImageMapper;

    /**
     * This repository is mocked in the com.rentacar.repository.search test package.
     *
     * @see com.rentacar.repository.search.CarImageSearchRepositoryMockConfiguration
     */
    @Autowired
    private CarImageSearchRepository mockCarImageSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCarImageMockMvc;

    private CarImage carImage;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CarImage createEntity(EntityManager em) {
        CarImage carImage = new CarImage().carId(DEFAULT_CAR_ID).imagePath(DEFAULT_IMAGE_PATH).date(DEFAULT_DATE);
        return carImage;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CarImage createUpdatedEntity(EntityManager em) {
        CarImage carImage = new CarImage().carId(UPDATED_CAR_ID).imagePath(UPDATED_IMAGE_PATH).date(UPDATED_DATE);
        return carImage;
    }

    @BeforeEach
    public void initTest() {
        carImage = createEntity(em);
    }

    @Test
    @Transactional
    void createCarImage() throws Exception {
        int databaseSizeBeforeCreate = carImageRepository.findAll().size();
        // Create the CarImage
        CarImageDTO carImageDTO = carImageMapper.toDto(carImage);
        restCarImageMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(carImageDTO)))
            .andExpect(status().isCreated());

        // Validate the CarImage in the database
        List<CarImage> carImageList = carImageRepository.findAll();
        assertThat(carImageList).hasSize(databaseSizeBeforeCreate + 1);
        CarImage testCarImage = carImageList.get(carImageList.size() - 1);
        assertThat(testCarImage.getCarId()).isEqualTo(DEFAULT_CAR_ID);
        assertThat(testCarImage.getImagePath()).isEqualTo(DEFAULT_IMAGE_PATH);
        assertThat(testCarImage.getDate()).isEqualTo(DEFAULT_DATE);

        // Validate the CarImage in Elasticsearch
        verify(mockCarImageSearchRepository, times(1)).save(testCarImage);
    }

    @Test
    @Transactional
    void createCarImageWithExistingId() throws Exception {
        // Create the CarImage with an existing ID
        carImage.setId(1L);
        CarImageDTO carImageDTO = carImageMapper.toDto(carImage);

        int databaseSizeBeforeCreate = carImageRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCarImageMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(carImageDTO)))
            .andExpect(status().isBadRequest());

        // Validate the CarImage in the database
        List<CarImage> carImageList = carImageRepository.findAll();
        assertThat(carImageList).hasSize(databaseSizeBeforeCreate);

        // Validate the CarImage in Elasticsearch
        verify(mockCarImageSearchRepository, times(0)).save(carImage);
    }

    @Test
    @Transactional
    void getAllCarImages() throws Exception {
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);

        // Get all the carImageList
        restCarImageMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(carImage.getId().intValue())))
            .andExpect(jsonPath("$.[*].carId").value(hasItem(DEFAULT_CAR_ID.intValue())))
            .andExpect(jsonPath("$.[*].imagePath").value(hasItem(DEFAULT_IMAGE_PATH)))
            .andExpect(jsonPath("$.[*].date").value(hasItem(sameInstant(DEFAULT_DATE))));
    }

    @Test
    @Transactional
    void getCarImage() throws Exception {
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);

        // Get the carImage
        restCarImageMockMvc
            .perform(get(ENTITY_API_URL_ID, carImage.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(carImage.getId().intValue()))
            .andExpect(jsonPath("$.carId").value(DEFAULT_CAR_ID.intValue()))
            .andExpect(jsonPath("$.imagePath").value(DEFAULT_IMAGE_PATH))
            .andExpect(jsonPath("$.date").value(sameInstant(DEFAULT_DATE)));
    }

    @Test
    @Transactional
    void getCarImagesByIdFiltering() throws Exception {
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);

        Long id = carImage.getId();

        defaultCarImageShouldBeFound("id.equals=" + id);
        defaultCarImageShouldNotBeFound("id.notEquals=" + id);

        defaultCarImageShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultCarImageShouldNotBeFound("id.greaterThan=" + id);

        defaultCarImageShouldBeFound("id.lessThanOrEqual=" + id);
        defaultCarImageShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllCarImagesByCarIdIsEqualToSomething() throws Exception {
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);

        // Get all the carImageList where carId equals to DEFAULT_CAR_ID
        defaultCarImageShouldBeFound("carId.equals=" + DEFAULT_CAR_ID);

        // Get all the carImageList where carId equals to UPDATED_CAR_ID
        defaultCarImageShouldNotBeFound("carId.equals=" + UPDATED_CAR_ID);
    }

    @Test
    @Transactional
    void getAllCarImagesByCarIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);

        // Get all the carImageList where carId not equals to DEFAULT_CAR_ID
        defaultCarImageShouldNotBeFound("carId.notEquals=" + DEFAULT_CAR_ID);

        // Get all the carImageList where carId not equals to UPDATED_CAR_ID
        defaultCarImageShouldBeFound("carId.notEquals=" + UPDATED_CAR_ID);
    }

    @Test
    @Transactional
    void getAllCarImagesByCarIdIsInShouldWork() throws Exception {
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);

        // Get all the carImageList where carId in DEFAULT_CAR_ID or UPDATED_CAR_ID
        defaultCarImageShouldBeFound("carId.in=" + DEFAULT_CAR_ID + "," + UPDATED_CAR_ID);

        // Get all the carImageList where carId equals to UPDATED_CAR_ID
        defaultCarImageShouldNotBeFound("carId.in=" + UPDATED_CAR_ID);
    }

    @Test
    @Transactional
    void getAllCarImagesByCarIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);

        // Get all the carImageList where carId is not null
        defaultCarImageShouldBeFound("carId.specified=true");

        // Get all the carImageList where carId is null
        defaultCarImageShouldNotBeFound("carId.specified=false");
    }

    @Test
    @Transactional
    void getAllCarImagesByCarIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);

        // Get all the carImageList where carId is greater than or equal to DEFAULT_CAR_ID
        defaultCarImageShouldBeFound("carId.greaterThanOrEqual=" + DEFAULT_CAR_ID);

        // Get all the carImageList where carId is greater than or equal to UPDATED_CAR_ID
        defaultCarImageShouldNotBeFound("carId.greaterThanOrEqual=" + UPDATED_CAR_ID);
    }

    @Test
    @Transactional
    void getAllCarImagesByCarIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);

        // Get all the carImageList where carId is less than or equal to DEFAULT_CAR_ID
        defaultCarImageShouldBeFound("carId.lessThanOrEqual=" + DEFAULT_CAR_ID);

        // Get all the carImageList where carId is less than or equal to SMALLER_CAR_ID
        defaultCarImageShouldNotBeFound("carId.lessThanOrEqual=" + SMALLER_CAR_ID);
    }

    @Test
    @Transactional
    void getAllCarImagesByCarIdIsLessThanSomething() throws Exception {
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);

        // Get all the carImageList where carId is less than DEFAULT_CAR_ID
        defaultCarImageShouldNotBeFound("carId.lessThan=" + DEFAULT_CAR_ID);

        // Get all the carImageList where carId is less than UPDATED_CAR_ID
        defaultCarImageShouldBeFound("carId.lessThan=" + UPDATED_CAR_ID);
    }

    @Test
    @Transactional
    void getAllCarImagesByCarIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);

        // Get all the carImageList where carId is greater than DEFAULT_CAR_ID
        defaultCarImageShouldNotBeFound("carId.greaterThan=" + DEFAULT_CAR_ID);

        // Get all the carImageList where carId is greater than SMALLER_CAR_ID
        defaultCarImageShouldBeFound("carId.greaterThan=" + SMALLER_CAR_ID);
    }

    @Test
    @Transactional
    void getAllCarImagesByImagePathIsEqualToSomething() throws Exception {
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);

        // Get all the carImageList where imagePath equals to DEFAULT_IMAGE_PATH
        defaultCarImageShouldBeFound("imagePath.equals=" + DEFAULT_IMAGE_PATH);

        // Get all the carImageList where imagePath equals to UPDATED_IMAGE_PATH
        defaultCarImageShouldNotBeFound("imagePath.equals=" + UPDATED_IMAGE_PATH);
    }

    @Test
    @Transactional
    void getAllCarImagesByImagePathIsNotEqualToSomething() throws Exception {
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);

        // Get all the carImageList where imagePath not equals to DEFAULT_IMAGE_PATH
        defaultCarImageShouldNotBeFound("imagePath.notEquals=" + DEFAULT_IMAGE_PATH);

        // Get all the carImageList where imagePath not equals to UPDATED_IMAGE_PATH
        defaultCarImageShouldBeFound("imagePath.notEquals=" + UPDATED_IMAGE_PATH);
    }

    @Test
    @Transactional
    void getAllCarImagesByImagePathIsInShouldWork() throws Exception {
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);

        // Get all the carImageList where imagePath in DEFAULT_IMAGE_PATH or UPDATED_IMAGE_PATH
        defaultCarImageShouldBeFound("imagePath.in=" + DEFAULT_IMAGE_PATH + "," + UPDATED_IMAGE_PATH);

        // Get all the carImageList where imagePath equals to UPDATED_IMAGE_PATH
        defaultCarImageShouldNotBeFound("imagePath.in=" + UPDATED_IMAGE_PATH);
    }

    @Test
    @Transactional
    void getAllCarImagesByImagePathIsNullOrNotNull() throws Exception {
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);

        // Get all the carImageList where imagePath is not null
        defaultCarImageShouldBeFound("imagePath.specified=true");

        // Get all the carImageList where imagePath is null
        defaultCarImageShouldNotBeFound("imagePath.specified=false");
    }

    @Test
    @Transactional
    void getAllCarImagesByImagePathContainsSomething() throws Exception {
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);

        // Get all the carImageList where imagePath contains DEFAULT_IMAGE_PATH
        defaultCarImageShouldBeFound("imagePath.contains=" + DEFAULT_IMAGE_PATH);

        // Get all the carImageList where imagePath contains UPDATED_IMAGE_PATH
        defaultCarImageShouldNotBeFound("imagePath.contains=" + UPDATED_IMAGE_PATH);
    }

    @Test
    @Transactional
    void getAllCarImagesByImagePathNotContainsSomething() throws Exception {
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);

        // Get all the carImageList where imagePath does not contain DEFAULT_IMAGE_PATH
        defaultCarImageShouldNotBeFound("imagePath.doesNotContain=" + DEFAULT_IMAGE_PATH);

        // Get all the carImageList where imagePath does not contain UPDATED_IMAGE_PATH
        defaultCarImageShouldBeFound("imagePath.doesNotContain=" + UPDATED_IMAGE_PATH);
    }

    @Test
    @Transactional
    void getAllCarImagesByDateIsEqualToSomething() throws Exception {
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);

        // Get all the carImageList where date equals to DEFAULT_DATE
        defaultCarImageShouldBeFound("date.equals=" + DEFAULT_DATE);

        // Get all the carImageList where date equals to UPDATED_DATE
        defaultCarImageShouldNotBeFound("date.equals=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    void getAllCarImagesByDateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);

        // Get all the carImageList where date not equals to DEFAULT_DATE
        defaultCarImageShouldNotBeFound("date.notEquals=" + DEFAULT_DATE);

        // Get all the carImageList where date not equals to UPDATED_DATE
        defaultCarImageShouldBeFound("date.notEquals=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    void getAllCarImagesByDateIsInShouldWork() throws Exception {
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);

        // Get all the carImageList where date in DEFAULT_DATE or UPDATED_DATE
        defaultCarImageShouldBeFound("date.in=" + DEFAULT_DATE + "," + UPDATED_DATE);

        // Get all the carImageList where date equals to UPDATED_DATE
        defaultCarImageShouldNotBeFound("date.in=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    void getAllCarImagesByDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);

        // Get all the carImageList where date is not null
        defaultCarImageShouldBeFound("date.specified=true");

        // Get all the carImageList where date is null
        defaultCarImageShouldNotBeFound("date.specified=false");
    }

    @Test
    @Transactional
    void getAllCarImagesByDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);

        // Get all the carImageList where date is greater than or equal to DEFAULT_DATE
        defaultCarImageShouldBeFound("date.greaterThanOrEqual=" + DEFAULT_DATE);

        // Get all the carImageList where date is greater than or equal to UPDATED_DATE
        defaultCarImageShouldNotBeFound("date.greaterThanOrEqual=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    void getAllCarImagesByDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);

        // Get all the carImageList where date is less than or equal to DEFAULT_DATE
        defaultCarImageShouldBeFound("date.lessThanOrEqual=" + DEFAULT_DATE);

        // Get all the carImageList where date is less than or equal to SMALLER_DATE
        defaultCarImageShouldNotBeFound("date.lessThanOrEqual=" + SMALLER_DATE);
    }

    @Test
    @Transactional
    void getAllCarImagesByDateIsLessThanSomething() throws Exception {
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);

        // Get all the carImageList where date is less than DEFAULT_DATE
        defaultCarImageShouldNotBeFound("date.lessThan=" + DEFAULT_DATE);

        // Get all the carImageList where date is less than UPDATED_DATE
        defaultCarImageShouldBeFound("date.lessThan=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    void getAllCarImagesByDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);

        // Get all the carImageList where date is greater than DEFAULT_DATE
        defaultCarImageShouldNotBeFound("date.greaterThan=" + DEFAULT_DATE);

        // Get all the carImageList where date is greater than SMALLER_DATE
        defaultCarImageShouldBeFound("date.greaterThan=" + SMALLER_DATE);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCarImageShouldBeFound(String filter) throws Exception {
        restCarImageMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(carImage.getId().intValue())))
            .andExpect(jsonPath("$.[*].carId").value(hasItem(DEFAULT_CAR_ID.intValue())))
            .andExpect(jsonPath("$.[*].imagePath").value(hasItem(DEFAULT_IMAGE_PATH)))
            .andExpect(jsonPath("$.[*].date").value(hasItem(sameInstant(DEFAULT_DATE))));

        // Check, that the count call also returns 1
        restCarImageMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCarImageShouldNotBeFound(String filter) throws Exception {
        restCarImageMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCarImageMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingCarImage() throws Exception {
        // Get the carImage
        restCarImageMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewCarImage() throws Exception {
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);

        int databaseSizeBeforeUpdate = carImageRepository.findAll().size();

        // Update the carImage
        CarImage updatedCarImage = carImageRepository.findById(carImage.getId()).get();
        // Disconnect from session so that the updates on updatedCarImage are not directly saved in db
        em.detach(updatedCarImage);
        updatedCarImage.carId(UPDATED_CAR_ID).imagePath(UPDATED_IMAGE_PATH).date(UPDATED_DATE);
        CarImageDTO carImageDTO = carImageMapper.toDto(updatedCarImage);

        restCarImageMockMvc
            .perform(
                put(ENTITY_API_URL_ID, carImageDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(carImageDTO))
            )
            .andExpect(status().isOk());

        // Validate the CarImage in the database
        List<CarImage> carImageList = carImageRepository.findAll();
        assertThat(carImageList).hasSize(databaseSizeBeforeUpdate);
        CarImage testCarImage = carImageList.get(carImageList.size() - 1);
        assertThat(testCarImage.getCarId()).isEqualTo(UPDATED_CAR_ID);
        assertThat(testCarImage.getImagePath()).isEqualTo(UPDATED_IMAGE_PATH);
        assertThat(testCarImage.getDate()).isEqualTo(UPDATED_DATE);

        // Validate the CarImage in Elasticsearch
        verify(mockCarImageSearchRepository).save(testCarImage);
    }

    @Test
    @Transactional
    void putNonExistingCarImage() throws Exception {
        int databaseSizeBeforeUpdate = carImageRepository.findAll().size();
        carImage.setId(count.incrementAndGet());

        // Create the CarImage
        CarImageDTO carImageDTO = carImageMapper.toDto(carImage);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCarImageMockMvc
            .perform(
                put(ENTITY_API_URL_ID, carImageDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(carImageDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CarImage in the database
        List<CarImage> carImageList = carImageRepository.findAll();
        assertThat(carImageList).hasSize(databaseSizeBeforeUpdate);

        // Validate the CarImage in Elasticsearch
        verify(mockCarImageSearchRepository, times(0)).save(carImage);
    }

    @Test
    @Transactional
    void putWithIdMismatchCarImage() throws Exception {
        int databaseSizeBeforeUpdate = carImageRepository.findAll().size();
        carImage.setId(count.incrementAndGet());

        // Create the CarImage
        CarImageDTO carImageDTO = carImageMapper.toDto(carImage);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCarImageMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(carImageDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CarImage in the database
        List<CarImage> carImageList = carImageRepository.findAll();
        assertThat(carImageList).hasSize(databaseSizeBeforeUpdate);

        // Validate the CarImage in Elasticsearch
        verify(mockCarImageSearchRepository, times(0)).save(carImage);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCarImage() throws Exception {
        int databaseSizeBeforeUpdate = carImageRepository.findAll().size();
        carImage.setId(count.incrementAndGet());

        // Create the CarImage
        CarImageDTO carImageDTO = carImageMapper.toDto(carImage);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCarImageMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(carImageDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the CarImage in the database
        List<CarImage> carImageList = carImageRepository.findAll();
        assertThat(carImageList).hasSize(databaseSizeBeforeUpdate);

        // Validate the CarImage in Elasticsearch
        verify(mockCarImageSearchRepository, times(0)).save(carImage);
    }

    @Test
    @Transactional
    void partialUpdateCarImageWithPatch() throws Exception {
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);

        int databaseSizeBeforeUpdate = carImageRepository.findAll().size();

        // Update the carImage using partial update
        CarImage partialUpdatedCarImage = new CarImage();
        partialUpdatedCarImage.setId(carImage.getId());

        partialUpdatedCarImage.carId(UPDATED_CAR_ID).date(UPDATED_DATE);

        restCarImageMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCarImage.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCarImage))
            )
            .andExpect(status().isOk());

        // Validate the CarImage in the database
        List<CarImage> carImageList = carImageRepository.findAll();
        assertThat(carImageList).hasSize(databaseSizeBeforeUpdate);
        CarImage testCarImage = carImageList.get(carImageList.size() - 1);
        assertThat(testCarImage.getCarId()).isEqualTo(UPDATED_CAR_ID);
        assertThat(testCarImage.getImagePath()).isEqualTo(DEFAULT_IMAGE_PATH);
        assertThat(testCarImage.getDate()).isEqualTo(UPDATED_DATE);
    }

    @Test
    @Transactional
    void fullUpdateCarImageWithPatch() throws Exception {
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);

        int databaseSizeBeforeUpdate = carImageRepository.findAll().size();

        // Update the carImage using partial update
        CarImage partialUpdatedCarImage = new CarImage();
        partialUpdatedCarImage.setId(carImage.getId());

        partialUpdatedCarImage.carId(UPDATED_CAR_ID).imagePath(UPDATED_IMAGE_PATH).date(UPDATED_DATE);

        restCarImageMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCarImage.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCarImage))
            )
            .andExpect(status().isOk());

        // Validate the CarImage in the database
        List<CarImage> carImageList = carImageRepository.findAll();
        assertThat(carImageList).hasSize(databaseSizeBeforeUpdate);
        CarImage testCarImage = carImageList.get(carImageList.size() - 1);
        assertThat(testCarImage.getCarId()).isEqualTo(UPDATED_CAR_ID);
        assertThat(testCarImage.getImagePath()).isEqualTo(UPDATED_IMAGE_PATH);
        assertThat(testCarImage.getDate()).isEqualTo(UPDATED_DATE);
    }

    @Test
    @Transactional
    void patchNonExistingCarImage() throws Exception {
        int databaseSizeBeforeUpdate = carImageRepository.findAll().size();
        carImage.setId(count.incrementAndGet());

        // Create the CarImage
        CarImageDTO carImageDTO = carImageMapper.toDto(carImage);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCarImageMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, carImageDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(carImageDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CarImage in the database
        List<CarImage> carImageList = carImageRepository.findAll();
        assertThat(carImageList).hasSize(databaseSizeBeforeUpdate);

        // Validate the CarImage in Elasticsearch
        verify(mockCarImageSearchRepository, times(0)).save(carImage);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCarImage() throws Exception {
        int databaseSizeBeforeUpdate = carImageRepository.findAll().size();
        carImage.setId(count.incrementAndGet());

        // Create the CarImage
        CarImageDTO carImageDTO = carImageMapper.toDto(carImage);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCarImageMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(carImageDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CarImage in the database
        List<CarImage> carImageList = carImageRepository.findAll();
        assertThat(carImageList).hasSize(databaseSizeBeforeUpdate);

        // Validate the CarImage in Elasticsearch
        verify(mockCarImageSearchRepository, times(0)).save(carImage);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCarImage() throws Exception {
        int databaseSizeBeforeUpdate = carImageRepository.findAll().size();
        carImage.setId(count.incrementAndGet());

        // Create the CarImage
        CarImageDTO carImageDTO = carImageMapper.toDto(carImage);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCarImageMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(carImageDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the CarImage in the database
        List<CarImage> carImageList = carImageRepository.findAll();
        assertThat(carImageList).hasSize(databaseSizeBeforeUpdate);

        // Validate the CarImage in Elasticsearch
        verify(mockCarImageSearchRepository, times(0)).save(carImage);
    }

    @Test
    @Transactional
    void deleteCarImage() throws Exception {
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);

        int databaseSizeBeforeDelete = carImageRepository.findAll().size();

        // Delete the carImage
        restCarImageMockMvc
            .perform(delete(ENTITY_API_URL_ID, carImage.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<CarImage> carImageList = carImageRepository.findAll();
        assertThat(carImageList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the CarImage in Elasticsearch
        verify(mockCarImageSearchRepository, times(1)).deleteById(carImage.getId());
    }

    @Test
    @Transactional
    void searchCarImage() throws Exception {
        // Configure the mock search repository
        // Initialize the database
        carImageRepository.saveAndFlush(carImage);
        when(mockCarImageSearchRepository.search(queryStringQuery("id:" + carImage.getId()), PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(Collections.singletonList(carImage), PageRequest.of(0, 1), 1));

        // Search the carImage
        restCarImageMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + carImage.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(carImage.getId().intValue())))
            .andExpect(jsonPath("$.[*].carId").value(hasItem(DEFAULT_CAR_ID.intValue())))
            .andExpect(jsonPath("$.[*].imagePath").value(hasItem(DEFAULT_IMAGE_PATH)))
            .andExpect(jsonPath("$.[*].date").value(hasItem(sameInstant(DEFAULT_DATE))));
    }
}
