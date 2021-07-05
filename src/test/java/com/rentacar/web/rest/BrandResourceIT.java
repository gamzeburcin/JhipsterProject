package com.rentacar.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.rentacar.IntegrationTest;
import com.rentacar.domain.Brand;
import com.rentacar.repository.BrandRepository;
import com.rentacar.repository.search.BrandSearchRepository;
import com.rentacar.service.criteria.BrandCriteria;
import com.rentacar.service.dto.BrandDTO;
import com.rentacar.service.mapper.BrandMapper;
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
 * Integration tests for the {@link BrandResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class BrandResourceIT {

    private static final Long DEFAULT_BRAND_ID = 1L;
    private static final Long UPDATED_BRAND_ID = 2L;
    private static final Long SMALLER_BRAND_ID = 1L - 1L;

    private static final String DEFAULT_BRAND_NAME = "AAAAAAAAAA";
    private static final String UPDATED_BRAND_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/brands";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/brands";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private BrandMapper brandMapper;

    /**
     * This repository is mocked in the com.rentacar.repository.search test package.
     *
     * @see com.rentacar.repository.search.BrandSearchRepositoryMockConfiguration
     */
    @Autowired
    private BrandSearchRepository mockBrandSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restBrandMockMvc;

    private Brand brand;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Brand createEntity(EntityManager em) {
        Brand brand = new Brand().brandId(DEFAULT_BRAND_ID).brandName(DEFAULT_BRAND_NAME);
        return brand;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Brand createUpdatedEntity(EntityManager em) {
        Brand brand = new Brand().brandId(UPDATED_BRAND_ID).brandName(UPDATED_BRAND_NAME);
        return brand;
    }

    @BeforeEach
    public void initTest() {
        brand = createEntity(em);
    }

    @Test
    @Transactional
    void createBrand() throws Exception {
        int databaseSizeBeforeCreate = brandRepository.findAll().size();
        // Create the Brand
        BrandDTO brandDTO = brandMapper.toDto(brand);
        restBrandMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(brandDTO)))
            .andExpect(status().isCreated());

        // Validate the Brand in the database
        List<Brand> brandList = brandRepository.findAll();
        assertThat(brandList).hasSize(databaseSizeBeforeCreate + 1);
        Brand testBrand = brandList.get(brandList.size() - 1);
        assertThat(testBrand.getBrandId()).isEqualTo(DEFAULT_BRAND_ID);
        assertThat(testBrand.getBrandName()).isEqualTo(DEFAULT_BRAND_NAME);

        // Validate the Brand in Elasticsearch
        verify(mockBrandSearchRepository, times(1)).save(testBrand);
    }

    @Test
    @Transactional
    void createBrandWithExistingId() throws Exception {
        // Create the Brand with an existing ID
        brand.setId(1L);
        BrandDTO brandDTO = brandMapper.toDto(brand);

        int databaseSizeBeforeCreate = brandRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restBrandMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(brandDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Brand in the database
        List<Brand> brandList = brandRepository.findAll();
        assertThat(brandList).hasSize(databaseSizeBeforeCreate);

        // Validate the Brand in Elasticsearch
        verify(mockBrandSearchRepository, times(0)).save(brand);
    }

    @Test
    @Transactional
    void getAllBrands() throws Exception {
        // Initialize the database
        brandRepository.saveAndFlush(brand);

        // Get all the brandList
        restBrandMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(brand.getId().intValue())))
            .andExpect(jsonPath("$.[*].brandId").value(hasItem(DEFAULT_BRAND_ID.intValue())))
            .andExpect(jsonPath("$.[*].brandName").value(hasItem(DEFAULT_BRAND_NAME)));
    }

    @Test
    @Transactional
    void getBrand() throws Exception {
        // Initialize the database
        brandRepository.saveAndFlush(brand);

        // Get the brand
        restBrandMockMvc
            .perform(get(ENTITY_API_URL_ID, brand.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(brand.getId().intValue()))
            .andExpect(jsonPath("$.brandId").value(DEFAULT_BRAND_ID.intValue()))
            .andExpect(jsonPath("$.brandName").value(DEFAULT_BRAND_NAME));
    }

    @Test
    @Transactional
    void getBrandsByIdFiltering() throws Exception {
        // Initialize the database
        brandRepository.saveAndFlush(brand);

        Long id = brand.getId();

        defaultBrandShouldBeFound("id.equals=" + id);
        defaultBrandShouldNotBeFound("id.notEquals=" + id);

        defaultBrandShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultBrandShouldNotBeFound("id.greaterThan=" + id);

        defaultBrandShouldBeFound("id.lessThanOrEqual=" + id);
        defaultBrandShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllBrandsByBrandIdIsEqualToSomething() throws Exception {
        // Initialize the database
        brandRepository.saveAndFlush(brand);

        // Get all the brandList where brandId equals to DEFAULT_BRAND_ID
        defaultBrandShouldBeFound("brandId.equals=" + DEFAULT_BRAND_ID);

        // Get all the brandList where brandId equals to UPDATED_BRAND_ID
        defaultBrandShouldNotBeFound("brandId.equals=" + UPDATED_BRAND_ID);
    }

    @Test
    @Transactional
    void getAllBrandsByBrandIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        brandRepository.saveAndFlush(brand);

        // Get all the brandList where brandId not equals to DEFAULT_BRAND_ID
        defaultBrandShouldNotBeFound("brandId.notEquals=" + DEFAULT_BRAND_ID);

        // Get all the brandList where brandId not equals to UPDATED_BRAND_ID
        defaultBrandShouldBeFound("brandId.notEquals=" + UPDATED_BRAND_ID);
    }

    @Test
    @Transactional
    void getAllBrandsByBrandIdIsInShouldWork() throws Exception {
        // Initialize the database
        brandRepository.saveAndFlush(brand);

        // Get all the brandList where brandId in DEFAULT_BRAND_ID or UPDATED_BRAND_ID
        defaultBrandShouldBeFound("brandId.in=" + DEFAULT_BRAND_ID + "," + UPDATED_BRAND_ID);

        // Get all the brandList where brandId equals to UPDATED_BRAND_ID
        defaultBrandShouldNotBeFound("brandId.in=" + UPDATED_BRAND_ID);
    }

    @Test
    @Transactional
    void getAllBrandsByBrandIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        brandRepository.saveAndFlush(brand);

        // Get all the brandList where brandId is not null
        defaultBrandShouldBeFound("brandId.specified=true");

        // Get all the brandList where brandId is null
        defaultBrandShouldNotBeFound("brandId.specified=false");
    }

    @Test
    @Transactional
    void getAllBrandsByBrandIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        brandRepository.saveAndFlush(brand);

        // Get all the brandList where brandId is greater than or equal to DEFAULT_BRAND_ID
        defaultBrandShouldBeFound("brandId.greaterThanOrEqual=" + DEFAULT_BRAND_ID);

        // Get all the brandList where brandId is greater than or equal to UPDATED_BRAND_ID
        defaultBrandShouldNotBeFound("brandId.greaterThanOrEqual=" + UPDATED_BRAND_ID);
    }

    @Test
    @Transactional
    void getAllBrandsByBrandIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        brandRepository.saveAndFlush(brand);

        // Get all the brandList where brandId is less than or equal to DEFAULT_BRAND_ID
        defaultBrandShouldBeFound("brandId.lessThanOrEqual=" + DEFAULT_BRAND_ID);

        // Get all the brandList where brandId is less than or equal to SMALLER_BRAND_ID
        defaultBrandShouldNotBeFound("brandId.lessThanOrEqual=" + SMALLER_BRAND_ID);
    }

    @Test
    @Transactional
    void getAllBrandsByBrandIdIsLessThanSomething() throws Exception {
        // Initialize the database
        brandRepository.saveAndFlush(brand);

        // Get all the brandList where brandId is less than DEFAULT_BRAND_ID
        defaultBrandShouldNotBeFound("brandId.lessThan=" + DEFAULT_BRAND_ID);

        // Get all the brandList where brandId is less than UPDATED_BRAND_ID
        defaultBrandShouldBeFound("brandId.lessThan=" + UPDATED_BRAND_ID);
    }

    @Test
    @Transactional
    void getAllBrandsByBrandIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        brandRepository.saveAndFlush(brand);

        // Get all the brandList where brandId is greater than DEFAULT_BRAND_ID
        defaultBrandShouldNotBeFound("brandId.greaterThan=" + DEFAULT_BRAND_ID);

        // Get all the brandList where brandId is greater than SMALLER_BRAND_ID
        defaultBrandShouldBeFound("brandId.greaterThan=" + SMALLER_BRAND_ID);
    }

    @Test
    @Transactional
    void getAllBrandsByBrandNameIsEqualToSomething() throws Exception {
        // Initialize the database
        brandRepository.saveAndFlush(brand);

        // Get all the brandList where brandName equals to DEFAULT_BRAND_NAME
        defaultBrandShouldBeFound("brandName.equals=" + DEFAULT_BRAND_NAME);

        // Get all the brandList where brandName equals to UPDATED_BRAND_NAME
        defaultBrandShouldNotBeFound("brandName.equals=" + UPDATED_BRAND_NAME);
    }

    @Test
    @Transactional
    void getAllBrandsByBrandNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        brandRepository.saveAndFlush(brand);

        // Get all the brandList where brandName not equals to DEFAULT_BRAND_NAME
        defaultBrandShouldNotBeFound("brandName.notEquals=" + DEFAULT_BRAND_NAME);

        // Get all the brandList where brandName not equals to UPDATED_BRAND_NAME
        defaultBrandShouldBeFound("brandName.notEquals=" + UPDATED_BRAND_NAME);
    }

    @Test
    @Transactional
    void getAllBrandsByBrandNameIsInShouldWork() throws Exception {
        // Initialize the database
        brandRepository.saveAndFlush(brand);

        // Get all the brandList where brandName in DEFAULT_BRAND_NAME or UPDATED_BRAND_NAME
        defaultBrandShouldBeFound("brandName.in=" + DEFAULT_BRAND_NAME + "," + UPDATED_BRAND_NAME);

        // Get all the brandList where brandName equals to UPDATED_BRAND_NAME
        defaultBrandShouldNotBeFound("brandName.in=" + UPDATED_BRAND_NAME);
    }

    @Test
    @Transactional
    void getAllBrandsByBrandNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        brandRepository.saveAndFlush(brand);

        // Get all the brandList where brandName is not null
        defaultBrandShouldBeFound("brandName.specified=true");

        // Get all the brandList where brandName is null
        defaultBrandShouldNotBeFound("brandName.specified=false");
    }

    @Test
    @Transactional
    void getAllBrandsByBrandNameContainsSomething() throws Exception {
        // Initialize the database
        brandRepository.saveAndFlush(brand);

        // Get all the brandList where brandName contains DEFAULT_BRAND_NAME
        defaultBrandShouldBeFound("brandName.contains=" + DEFAULT_BRAND_NAME);

        // Get all the brandList where brandName contains UPDATED_BRAND_NAME
        defaultBrandShouldNotBeFound("brandName.contains=" + UPDATED_BRAND_NAME);
    }

    @Test
    @Transactional
    void getAllBrandsByBrandNameNotContainsSomething() throws Exception {
        // Initialize the database
        brandRepository.saveAndFlush(brand);

        // Get all the brandList where brandName does not contain DEFAULT_BRAND_NAME
        defaultBrandShouldNotBeFound("brandName.doesNotContain=" + DEFAULT_BRAND_NAME);

        // Get all the brandList where brandName does not contain UPDATED_BRAND_NAME
        defaultBrandShouldBeFound("brandName.doesNotContain=" + UPDATED_BRAND_NAME);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultBrandShouldBeFound(String filter) throws Exception {
        restBrandMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(brand.getId().intValue())))
            .andExpect(jsonPath("$.[*].brandId").value(hasItem(DEFAULT_BRAND_ID.intValue())))
            .andExpect(jsonPath("$.[*].brandName").value(hasItem(DEFAULT_BRAND_NAME)));

        // Check, that the count call also returns 1
        restBrandMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultBrandShouldNotBeFound(String filter) throws Exception {
        restBrandMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restBrandMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingBrand() throws Exception {
        // Get the brand
        restBrandMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewBrand() throws Exception {
        // Initialize the database
        brandRepository.saveAndFlush(brand);

        int databaseSizeBeforeUpdate = brandRepository.findAll().size();

        // Update the brand
        Brand updatedBrand = brandRepository.findById(brand.getId()).get();
        // Disconnect from session so that the updates on updatedBrand are not directly saved in db
        em.detach(updatedBrand);
        updatedBrand.brandId(UPDATED_BRAND_ID).brandName(UPDATED_BRAND_NAME);
        BrandDTO brandDTO = brandMapper.toDto(updatedBrand);

        restBrandMockMvc
            .perform(
                put(ENTITY_API_URL_ID, brandDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(brandDTO))
            )
            .andExpect(status().isOk());

        // Validate the Brand in the database
        List<Brand> brandList = brandRepository.findAll();
        assertThat(brandList).hasSize(databaseSizeBeforeUpdate);
        Brand testBrand = brandList.get(brandList.size() - 1);
        assertThat(testBrand.getBrandId()).isEqualTo(UPDATED_BRAND_ID);
        assertThat(testBrand.getBrandName()).isEqualTo(UPDATED_BRAND_NAME);

        // Validate the Brand in Elasticsearch
        verify(mockBrandSearchRepository).save(testBrand);
    }

    @Test
    @Transactional
    void putNonExistingBrand() throws Exception {
        int databaseSizeBeforeUpdate = brandRepository.findAll().size();
        brand.setId(count.incrementAndGet());

        // Create the Brand
        BrandDTO brandDTO = brandMapper.toDto(brand);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBrandMockMvc
            .perform(
                put(ENTITY_API_URL_ID, brandDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(brandDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Brand in the database
        List<Brand> brandList = brandRepository.findAll();
        assertThat(brandList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Brand in Elasticsearch
        verify(mockBrandSearchRepository, times(0)).save(brand);
    }

    @Test
    @Transactional
    void putWithIdMismatchBrand() throws Exception {
        int databaseSizeBeforeUpdate = brandRepository.findAll().size();
        brand.setId(count.incrementAndGet());

        // Create the Brand
        BrandDTO brandDTO = brandMapper.toDto(brand);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBrandMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(brandDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Brand in the database
        List<Brand> brandList = brandRepository.findAll();
        assertThat(brandList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Brand in Elasticsearch
        verify(mockBrandSearchRepository, times(0)).save(brand);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamBrand() throws Exception {
        int databaseSizeBeforeUpdate = brandRepository.findAll().size();
        brand.setId(count.incrementAndGet());

        // Create the Brand
        BrandDTO brandDTO = brandMapper.toDto(brand);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBrandMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(brandDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Brand in the database
        List<Brand> brandList = brandRepository.findAll();
        assertThat(brandList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Brand in Elasticsearch
        verify(mockBrandSearchRepository, times(0)).save(brand);
    }

    @Test
    @Transactional
    void partialUpdateBrandWithPatch() throws Exception {
        // Initialize the database
        brandRepository.saveAndFlush(brand);

        int databaseSizeBeforeUpdate = brandRepository.findAll().size();

        // Update the brand using partial update
        Brand partialUpdatedBrand = new Brand();
        partialUpdatedBrand.setId(brand.getId());

        partialUpdatedBrand.brandId(UPDATED_BRAND_ID);

        restBrandMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBrand.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedBrand))
            )
            .andExpect(status().isOk());

        // Validate the Brand in the database
        List<Brand> brandList = brandRepository.findAll();
        assertThat(brandList).hasSize(databaseSizeBeforeUpdate);
        Brand testBrand = brandList.get(brandList.size() - 1);
        assertThat(testBrand.getBrandId()).isEqualTo(UPDATED_BRAND_ID);
        assertThat(testBrand.getBrandName()).isEqualTo(DEFAULT_BRAND_NAME);
    }

    @Test
    @Transactional
    void fullUpdateBrandWithPatch() throws Exception {
        // Initialize the database
        brandRepository.saveAndFlush(brand);

        int databaseSizeBeforeUpdate = brandRepository.findAll().size();

        // Update the brand using partial update
        Brand partialUpdatedBrand = new Brand();
        partialUpdatedBrand.setId(brand.getId());

        partialUpdatedBrand.brandId(UPDATED_BRAND_ID).brandName(UPDATED_BRAND_NAME);

        restBrandMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBrand.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedBrand))
            )
            .andExpect(status().isOk());

        // Validate the Brand in the database
        List<Brand> brandList = brandRepository.findAll();
        assertThat(brandList).hasSize(databaseSizeBeforeUpdate);
        Brand testBrand = brandList.get(brandList.size() - 1);
        assertThat(testBrand.getBrandId()).isEqualTo(UPDATED_BRAND_ID);
        assertThat(testBrand.getBrandName()).isEqualTo(UPDATED_BRAND_NAME);
    }

    @Test
    @Transactional
    void patchNonExistingBrand() throws Exception {
        int databaseSizeBeforeUpdate = brandRepository.findAll().size();
        brand.setId(count.incrementAndGet());

        // Create the Brand
        BrandDTO brandDTO = brandMapper.toDto(brand);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBrandMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, brandDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(brandDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Brand in the database
        List<Brand> brandList = brandRepository.findAll();
        assertThat(brandList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Brand in Elasticsearch
        verify(mockBrandSearchRepository, times(0)).save(brand);
    }

    @Test
    @Transactional
    void patchWithIdMismatchBrand() throws Exception {
        int databaseSizeBeforeUpdate = brandRepository.findAll().size();
        brand.setId(count.incrementAndGet());

        // Create the Brand
        BrandDTO brandDTO = brandMapper.toDto(brand);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBrandMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(brandDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Brand in the database
        List<Brand> brandList = brandRepository.findAll();
        assertThat(brandList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Brand in Elasticsearch
        verify(mockBrandSearchRepository, times(0)).save(brand);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamBrand() throws Exception {
        int databaseSizeBeforeUpdate = brandRepository.findAll().size();
        brand.setId(count.incrementAndGet());

        // Create the Brand
        BrandDTO brandDTO = brandMapper.toDto(brand);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBrandMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(brandDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Brand in the database
        List<Brand> brandList = brandRepository.findAll();
        assertThat(brandList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Brand in Elasticsearch
        verify(mockBrandSearchRepository, times(0)).save(brand);
    }

    @Test
    @Transactional
    void deleteBrand() throws Exception {
        // Initialize the database
        brandRepository.saveAndFlush(brand);

        int databaseSizeBeforeDelete = brandRepository.findAll().size();

        // Delete the brand
        restBrandMockMvc
            .perform(delete(ENTITY_API_URL_ID, brand.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Brand> brandList = brandRepository.findAll();
        assertThat(brandList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Brand in Elasticsearch
        verify(mockBrandSearchRepository, times(1)).deleteById(brand.getId());
    }

    @Test
    @Transactional
    void searchBrand() throws Exception {
        // Configure the mock search repository
        // Initialize the database
        brandRepository.saveAndFlush(brand);
        when(mockBrandSearchRepository.search(queryStringQuery("id:" + brand.getId()), PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(Collections.singletonList(brand), PageRequest.of(0, 1), 1));

        // Search the brand
        restBrandMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + brand.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(brand.getId().intValue())))
            .andExpect(jsonPath("$.[*].brandId").value(hasItem(DEFAULT_BRAND_ID.intValue())))
            .andExpect(jsonPath("$.[*].brandName").value(hasItem(DEFAULT_BRAND_NAME)));
    }
}
