package com.rentacar.service;

import com.rentacar.service.dto.CarImageDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.rentacar.domain.CarImage}.
 */
public interface CarImageService {
    /**
     * Save a carImage.
     *
     * @param carImageDTO the entity to save.
     * @return the persisted entity.
     */
    CarImageDTO save(CarImageDTO carImageDTO);

    /**
     * Partially updates a carImage.
     *
     * @param carImageDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<CarImageDTO> partialUpdate(CarImageDTO carImageDTO);

    /**
     * Get all the carImages.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CarImageDTO> findAll(Pageable pageable);

    /**
     * Get the "id" carImage.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CarImageDTO> findOne(Long id);

    /**
     * Delete the "id" carImage.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Search for the carImage corresponding to the query.
     *
     * @param query the query of the search.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CarImageDTO> search(String query, Pageable pageable);
}
