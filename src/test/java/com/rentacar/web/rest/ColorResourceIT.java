package com.rentacar.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.rentacar.IntegrationTest;
import com.rentacar.domain.Color;
import com.rentacar.repository.ColorRepository;
import com.rentacar.repository.search.ColorSearchRepository;
import com.rentacar.service.criteria.ColorCriteria;
import com.rentacar.service.dto.ColorDTO;
import com.rentacar.service.mapper.ColorMapper;
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
 * Integration tests for the {@link ColorResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class ColorResourceIT {

    private static final String DEFAULT_COLOR_NAME = "AAAAAAAAAA";
    private static final String UPDATED_COLOR_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/colors";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/colors";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ColorRepository colorRepository;

    @Autowired
    private ColorMapper colorMapper;

    /**
     * This repository is mocked in the com.rentacar.repository.search test package.
     *
     * @see com.rentacar.repository.search.ColorSearchRepositoryMockConfiguration
     */
    @Autowired
    private ColorSearchRepository mockColorSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restColorMockMvc;

    private Color color;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Color createEntity(EntityManager em) {
        Color color = new Color().colorName(DEFAULT_COLOR_NAME);
        return color;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Color createUpdatedEntity(EntityManager em) {
        Color color = new Color().colorName(UPDATED_COLOR_NAME);
        return color;
    }

    @BeforeEach
    public void initTest() {
        color = createEntity(em);
    }

    @Test
    @Transactional
    void createColor() throws Exception {
        int databaseSizeBeforeCreate = colorRepository.findAll().size();
        // Create the Color
        ColorDTO colorDTO = colorMapper.toDto(color);
        restColorMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(colorDTO)))
            .andExpect(status().isCreated());

        // Validate the Color in the database
        List<Color> colorList = colorRepository.findAll();
        assertThat(colorList).hasSize(databaseSizeBeforeCreate + 1);
        Color testColor = colorList.get(colorList.size() - 1);
        assertThat(testColor.getColorName()).isEqualTo(DEFAULT_COLOR_NAME);

        // Validate the Color in Elasticsearch
        verify(mockColorSearchRepository, times(1)).save(testColor);
    }

    @Test
    @Transactional
    void createColorWithExistingId() throws Exception {
        // Create the Color with an existing ID
        color.setId(1L);
        ColorDTO colorDTO = colorMapper.toDto(color);

        int databaseSizeBeforeCreate = colorRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restColorMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(colorDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Color in the database
        List<Color> colorList = colorRepository.findAll();
        assertThat(colorList).hasSize(databaseSizeBeforeCreate);

        // Validate the Color in Elasticsearch
        verify(mockColorSearchRepository, times(0)).save(color);
    }

    @Test
    @Transactional
    void getAllColors() throws Exception {
        // Initialize the database
        colorRepository.saveAndFlush(color);

        // Get all the colorList
        restColorMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(color.getId().intValue())))
            .andExpect(jsonPath("$.[*].colorName").value(hasItem(DEFAULT_COLOR_NAME)));
    }

    @Test
    @Transactional
    void getColor() throws Exception {
        // Initialize the database
        colorRepository.saveAndFlush(color);

        // Get the color
        restColorMockMvc
            .perform(get(ENTITY_API_URL_ID, color.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(color.getId().intValue()))
            .andExpect(jsonPath("$.colorName").value(DEFAULT_COLOR_NAME));
    }

    @Test
    @Transactional
    void getColorsByIdFiltering() throws Exception {
        // Initialize the database
        colorRepository.saveAndFlush(color);

        Long id = color.getId();

        defaultColorShouldBeFound("id.equals=" + id);
        defaultColorShouldNotBeFound("id.notEquals=" + id);

        defaultColorShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultColorShouldNotBeFound("id.greaterThan=" + id);

        defaultColorShouldBeFound("id.lessThanOrEqual=" + id);
        defaultColorShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllColorsByColorNameIsEqualToSomething() throws Exception {
        // Initialize the database
        colorRepository.saveAndFlush(color);

        // Get all the colorList where colorName equals to DEFAULT_COLOR_NAME
        defaultColorShouldBeFound("colorName.equals=" + DEFAULT_COLOR_NAME);

        // Get all the colorList where colorName equals to UPDATED_COLOR_NAME
        defaultColorShouldNotBeFound("colorName.equals=" + UPDATED_COLOR_NAME);
    }

    @Test
    @Transactional
    void getAllColorsByColorNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        colorRepository.saveAndFlush(color);

        // Get all the colorList where colorName not equals to DEFAULT_COLOR_NAME
        defaultColorShouldNotBeFound("colorName.notEquals=" + DEFAULT_COLOR_NAME);

        // Get all the colorList where colorName not equals to UPDATED_COLOR_NAME
        defaultColorShouldBeFound("colorName.notEquals=" + UPDATED_COLOR_NAME);
    }

    @Test
    @Transactional
    void getAllColorsByColorNameIsInShouldWork() throws Exception {
        // Initialize the database
        colorRepository.saveAndFlush(color);

        // Get all the colorList where colorName in DEFAULT_COLOR_NAME or UPDATED_COLOR_NAME
        defaultColorShouldBeFound("colorName.in=" + DEFAULT_COLOR_NAME + "," + UPDATED_COLOR_NAME);

        // Get all the colorList where colorName equals to UPDATED_COLOR_NAME
        defaultColorShouldNotBeFound("colorName.in=" + UPDATED_COLOR_NAME);
    }

    @Test
    @Transactional
    void getAllColorsByColorNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        colorRepository.saveAndFlush(color);

        // Get all the colorList where colorName is not null
        defaultColorShouldBeFound("colorName.specified=true");

        // Get all the colorList where colorName is null
        defaultColorShouldNotBeFound("colorName.specified=false");
    }

    @Test
    @Transactional
    void getAllColorsByColorNameContainsSomething() throws Exception {
        // Initialize the database
        colorRepository.saveAndFlush(color);

        // Get all the colorList where colorName contains DEFAULT_COLOR_NAME
        defaultColorShouldBeFound("colorName.contains=" + DEFAULT_COLOR_NAME);

        // Get all the colorList where colorName contains UPDATED_COLOR_NAME
        defaultColorShouldNotBeFound("colorName.contains=" + UPDATED_COLOR_NAME);
    }

    @Test
    @Transactional
    void getAllColorsByColorNameNotContainsSomething() throws Exception {
        // Initialize the database
        colorRepository.saveAndFlush(color);

        // Get all the colorList where colorName does not contain DEFAULT_COLOR_NAME
        defaultColorShouldNotBeFound("colorName.doesNotContain=" + DEFAULT_COLOR_NAME);

        // Get all the colorList where colorName does not contain UPDATED_COLOR_NAME
        defaultColorShouldBeFound("colorName.doesNotContain=" + UPDATED_COLOR_NAME);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultColorShouldBeFound(String filter) throws Exception {
        restColorMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(color.getId().intValue())))
            .andExpect(jsonPath("$.[*].colorName").value(hasItem(DEFAULT_COLOR_NAME)));

        // Check, that the count call also returns 1
        restColorMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultColorShouldNotBeFound(String filter) throws Exception {
        restColorMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restColorMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingColor() throws Exception {
        // Get the color
        restColorMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewColor() throws Exception {
        // Initialize the database
        colorRepository.saveAndFlush(color);

        int databaseSizeBeforeUpdate = colorRepository.findAll().size();

        // Update the color
        Color updatedColor = colorRepository.findById(color.getId()).get();
        // Disconnect from session so that the updates on updatedColor are not directly saved in db
        em.detach(updatedColor);
        updatedColor.colorName(UPDATED_COLOR_NAME);
        ColorDTO colorDTO = colorMapper.toDto(updatedColor);

        restColorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, colorDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(colorDTO))
            )
            .andExpect(status().isOk());

        // Validate the Color in the database
        List<Color> colorList = colorRepository.findAll();
        assertThat(colorList).hasSize(databaseSizeBeforeUpdate);
        Color testColor = colorList.get(colorList.size() - 1);
        assertThat(testColor.getColorName()).isEqualTo(UPDATED_COLOR_NAME);

        // Validate the Color in Elasticsearch
        verify(mockColorSearchRepository).save(testColor);
    }

    @Test
    @Transactional
    void putNonExistingColor() throws Exception {
        int databaseSizeBeforeUpdate = colorRepository.findAll().size();
        color.setId(count.incrementAndGet());

        // Create the Color
        ColorDTO colorDTO = colorMapper.toDto(color);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restColorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, colorDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(colorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Color in the database
        List<Color> colorList = colorRepository.findAll();
        assertThat(colorList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Color in Elasticsearch
        verify(mockColorSearchRepository, times(0)).save(color);
    }

    @Test
    @Transactional
    void putWithIdMismatchColor() throws Exception {
        int databaseSizeBeforeUpdate = colorRepository.findAll().size();
        color.setId(count.incrementAndGet());

        // Create the Color
        ColorDTO colorDTO = colorMapper.toDto(color);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restColorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(colorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Color in the database
        List<Color> colorList = colorRepository.findAll();
        assertThat(colorList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Color in Elasticsearch
        verify(mockColorSearchRepository, times(0)).save(color);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamColor() throws Exception {
        int databaseSizeBeforeUpdate = colorRepository.findAll().size();
        color.setId(count.incrementAndGet());

        // Create the Color
        ColorDTO colorDTO = colorMapper.toDto(color);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restColorMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(colorDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Color in the database
        List<Color> colorList = colorRepository.findAll();
        assertThat(colorList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Color in Elasticsearch
        verify(mockColorSearchRepository, times(0)).save(color);
    }

    @Test
    @Transactional
    void partialUpdateColorWithPatch() throws Exception {
        // Initialize the database
        colorRepository.saveAndFlush(color);

        int databaseSizeBeforeUpdate = colorRepository.findAll().size();

        // Update the color using partial update
        Color partialUpdatedColor = new Color();
        partialUpdatedColor.setId(color.getId());

        restColorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedColor.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedColor))
            )
            .andExpect(status().isOk());

        // Validate the Color in the database
        List<Color> colorList = colorRepository.findAll();
        assertThat(colorList).hasSize(databaseSizeBeforeUpdate);
        Color testColor = colorList.get(colorList.size() - 1);
        assertThat(testColor.getColorName()).isEqualTo(DEFAULT_COLOR_NAME);
    }

    @Test
    @Transactional
    void fullUpdateColorWithPatch() throws Exception {
        // Initialize the database
        colorRepository.saveAndFlush(color);

        int databaseSizeBeforeUpdate = colorRepository.findAll().size();

        // Update the color using partial update
        Color partialUpdatedColor = new Color();
        partialUpdatedColor.setId(color.getId());

        partialUpdatedColor.colorName(UPDATED_COLOR_NAME);

        restColorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedColor.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedColor))
            )
            .andExpect(status().isOk());

        // Validate the Color in the database
        List<Color> colorList = colorRepository.findAll();
        assertThat(colorList).hasSize(databaseSizeBeforeUpdate);
        Color testColor = colorList.get(colorList.size() - 1);
        assertThat(testColor.getColorName()).isEqualTo(UPDATED_COLOR_NAME);
    }

    @Test
    @Transactional
    void patchNonExistingColor() throws Exception {
        int databaseSizeBeforeUpdate = colorRepository.findAll().size();
        color.setId(count.incrementAndGet());

        // Create the Color
        ColorDTO colorDTO = colorMapper.toDto(color);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restColorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, colorDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(colorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Color in the database
        List<Color> colorList = colorRepository.findAll();
        assertThat(colorList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Color in Elasticsearch
        verify(mockColorSearchRepository, times(0)).save(color);
    }

    @Test
    @Transactional
    void patchWithIdMismatchColor() throws Exception {
        int databaseSizeBeforeUpdate = colorRepository.findAll().size();
        color.setId(count.incrementAndGet());

        // Create the Color
        ColorDTO colorDTO = colorMapper.toDto(color);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restColorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(colorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Color in the database
        List<Color> colorList = colorRepository.findAll();
        assertThat(colorList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Color in Elasticsearch
        verify(mockColorSearchRepository, times(0)).save(color);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamColor() throws Exception {
        int databaseSizeBeforeUpdate = colorRepository.findAll().size();
        color.setId(count.incrementAndGet());

        // Create the Color
        ColorDTO colorDTO = colorMapper.toDto(color);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restColorMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(colorDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Color in the database
        List<Color> colorList = colorRepository.findAll();
        assertThat(colorList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Color in Elasticsearch
        verify(mockColorSearchRepository, times(0)).save(color);
    }

    @Test
    @Transactional
    void deleteColor() throws Exception {
        // Initialize the database
        colorRepository.saveAndFlush(color);

        int databaseSizeBeforeDelete = colorRepository.findAll().size();

        // Delete the color
        restColorMockMvc
            .perform(delete(ENTITY_API_URL_ID, color.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Color> colorList = colorRepository.findAll();
        assertThat(colorList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Color in Elasticsearch
        verify(mockColorSearchRepository, times(1)).deleteById(color.getId());
    }

    @Test
    @Transactional
    void searchColor() throws Exception {
        // Configure the mock search repository
        // Initialize the database
        colorRepository.saveAndFlush(color);
        when(mockColorSearchRepository.search(queryStringQuery("id:" + color.getId()), PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(Collections.singletonList(color), PageRequest.of(0, 1), 1));

        // Search the color
        restColorMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + color.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(color.getId().intValue())))
            .andExpect(jsonPath("$.[*].colorName").value(hasItem(DEFAULT_COLOR_NAME)));
    }
}
