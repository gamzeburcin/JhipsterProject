package com.rentacar.service;

import com.rentacar.domain.*; // for static metamodels
import com.rentacar.domain.CarImage;
import com.rentacar.repository.CarImageRepository;
import com.rentacar.repository.search.CarImageSearchRepository;
import com.rentacar.service.criteria.CarImageCriteria;
import com.rentacar.service.dto.CarImageDTO;
import com.rentacar.service.mapper.CarImageMapper;
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
 * Service for executing complex queries for {@link CarImage} entities in the database.
 * The main input is a {@link CarImageCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link CarImageDTO} or a {@link Page} of {@link CarImageDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CarImageQueryService extends QueryService<CarImage> {

    private final Logger log = LoggerFactory.getLogger(CarImageQueryService.class);

    private final CarImageRepository carImageRepository;

    private final CarImageMapper carImageMapper;

    private final CarImageSearchRepository carImageSearchRepository;

    public CarImageQueryService(
        CarImageRepository carImageRepository,
        CarImageMapper carImageMapper,
        CarImageSearchRepository carImageSearchRepository
    ) {
        this.carImageRepository = carImageRepository;
        this.carImageMapper = carImageMapper;
        this.carImageSearchRepository = carImageSearchRepository;
    }

    /**
     * Return a {@link List} of {@link CarImageDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<CarImageDTO> findByCriteria(CarImageCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<CarImage> specification = createSpecification(criteria);
        return carImageMapper.toDto(carImageRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link CarImageDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<CarImageDTO> findByCriteria(CarImageCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<CarImage> specification = createSpecification(criteria);
        return carImageRepository.findAll(specification, page).map(carImageMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CarImageCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<CarImage> specification = createSpecification(criteria);
        return carImageRepository.count(specification);
    }

    /**
     * Function to convert {@link CarImageCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<CarImage> createSpecification(CarImageCriteria criteria) {
        Specification<CarImage> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), CarImage_.id));
            }
            if (criteria.getCarId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCarId(), CarImage_.carId));
            }
            if (criteria.getImagePath() != null) {
                specification = specification.and(buildStringSpecification(criteria.getImagePath(), CarImage_.imagePath));
            }
            if (criteria.getDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getDate(), CarImage_.date));
            }
        }
        return specification;
    }
}
