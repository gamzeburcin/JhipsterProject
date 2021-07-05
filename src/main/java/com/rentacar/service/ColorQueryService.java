package com.rentacar.service;

import com.rentacar.domain.*; // for static metamodels
import com.rentacar.domain.Color;
import com.rentacar.repository.ColorRepository;
import com.rentacar.repository.search.ColorSearchRepository;
import com.rentacar.service.criteria.ColorCriteria;
import com.rentacar.service.dto.ColorDTO;
import com.rentacar.service.mapper.ColorMapper;
import java.util.List;
import javax.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Color} entities in the database.
 * The main input is a {@link ColorCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link ColorDTO} or a {@link Page} of {@link ColorDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ColorQueryService extends QueryService<Color> {

    private final Logger log = LoggerFactory.getLogger(ColorQueryService.class);

    private final ColorRepository colorRepository;

    private final ColorMapper colorMapper;

    private final ColorSearchRepository colorSearchRepository;

    public ColorQueryService(ColorRepository colorRepository, ColorMapper colorMapper, ColorSearchRepository colorSearchRepository) {
        this.colorRepository = colorRepository;
        this.colorMapper = colorMapper;
        this.colorSearchRepository = colorSearchRepository;
    }

    /**
     * Return a {@link List} of {@link ColorDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<ColorDTO> findByCriteria(ColorCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Color> specification = createSpecification(criteria);
        return colorMapper.toDto(colorRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link ColorDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ColorDTO> findByCriteria(ColorCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Color> specification = createSpecification(criteria);
        return colorRepository.findAll(specification, page).map(colorMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ColorCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Color> specification = createSpecification(criteria);
        return colorRepository.count(specification);
    }

    /**
     * Function to convert {@link ColorCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Color> createSpecification(ColorCriteria criteria) {
        Specification<Color> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Color_.id));
            }
            if (criteria.getColorName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getColorName(), Color_.colorName));
            }
        }
        return specification;
    }
}
