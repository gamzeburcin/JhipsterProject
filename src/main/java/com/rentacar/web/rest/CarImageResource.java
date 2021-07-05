package com.rentacar.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.rentacar.repository.CarImageRepository;
import com.rentacar.service.CarImageQueryService;
import com.rentacar.service.CarImageService;
import com.rentacar.service.criteria.CarImageCriteria;
import com.rentacar.service.dto.CarImageDTO;
import com.rentacar.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.rentacar.domain.CarImage}.
 */
@RestController
@RequestMapping("/api")
public class CarImageResource {

    private final Logger log = LoggerFactory.getLogger(CarImageResource.class);

    private static final String ENTITY_NAME = "carImage";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CarImageService carImageService;

    private final CarImageRepository carImageRepository;

    private final CarImageQueryService carImageQueryService;

    public CarImageResource(
        CarImageService carImageService,
        CarImageRepository carImageRepository,
        CarImageQueryService carImageQueryService
    ) {
        this.carImageService = carImageService;
        this.carImageRepository = carImageRepository;
        this.carImageQueryService = carImageQueryService;
    }

    /**
     * {@code POST  /car-images} : Create a new carImage.
     *
     * @param carImageDTO the carImageDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new carImageDTO, or with status {@code 400 (Bad Request)} if the carImage has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/car-images")
    public ResponseEntity<CarImageDTO> createCarImage(@RequestBody CarImageDTO carImageDTO) throws URISyntaxException {
        log.debug("REST request to save CarImage : {}", carImageDTO);
        if (carImageDTO.getId() != null) {
            throw new BadRequestAlertException("A new carImage cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CarImageDTO result = carImageService.save(carImageDTO);
        return ResponseEntity
            .created(new URI("/api/car-images/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /car-images/:id} : Updates an existing carImage.
     *
     * @param id the id of the carImageDTO to save.
     * @param carImageDTO the carImageDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated carImageDTO,
     * or with status {@code 400 (Bad Request)} if the carImageDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the carImageDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/car-images/{id}")
    public ResponseEntity<CarImageDTO> updateCarImage(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody CarImageDTO carImageDTO
    ) throws URISyntaxException {
        log.debug("REST request to update CarImage : {}, {}", id, carImageDTO);
        if (carImageDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, carImageDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!carImageRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        CarImageDTO result = carImageService.save(carImageDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, carImageDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /car-images/:id} : Partial updates given fields of an existing carImage, field will ignore if it is null
     *
     * @param id the id of the carImageDTO to save.
     * @param carImageDTO the carImageDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated carImageDTO,
     * or with status {@code 400 (Bad Request)} if the carImageDTO is not valid,
     * or with status {@code 404 (Not Found)} if the carImageDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the carImageDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/car-images/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<CarImageDTO> partialUpdateCarImage(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody CarImageDTO carImageDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update CarImage partially : {}, {}", id, carImageDTO);
        if (carImageDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, carImageDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!carImageRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CarImageDTO> result = carImageService.partialUpdate(carImageDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, carImageDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /car-images} : get all the carImages.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of carImages in body.
     */
    @GetMapping("/car-images")
    public ResponseEntity<List<CarImageDTO>> getAllCarImages(CarImageCriteria criteria, Pageable pageable) {
        log.debug("REST request to get CarImages by criteria: {}", criteria);
        Page<CarImageDTO> page = carImageQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /car-images/count} : count all the carImages.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/car-images/count")
    public ResponseEntity<Long> countCarImages(CarImageCriteria criteria) {
        log.debug("REST request to count CarImages by criteria: {}", criteria);
        return ResponseEntity.ok().body(carImageQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /car-images/:id} : get the "id" carImage.
     *
     * @param id the id of the carImageDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the carImageDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/car-images/{id}")
    public ResponseEntity<CarImageDTO> getCarImage(@PathVariable Long id) {
        log.debug("REST request to get CarImage : {}", id);
        Optional<CarImageDTO> carImageDTO = carImageService.findOne(id);
        return ResponseUtil.wrapOrNotFound(carImageDTO);
    }

    /**
     * {@code DELETE  /car-images/:id} : delete the "id" carImage.
     *
     * @param id the id of the carImageDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/car-images/{id}")
    public ResponseEntity<Void> deleteCarImage(@PathVariable Long id) {
        log.debug("REST request to delete CarImage : {}", id);
        carImageService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /_search/car-images?query=:query} : search for the carImage corresponding
     * to the query.
     *
     * @param query the query of the carImage search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search/car-images")
    public ResponseEntity<List<CarImageDTO>> searchCarImages(@RequestParam String query, Pageable pageable) {
        log.debug("REST request to search for a page of CarImages for query {}", query);
        Page<CarImageDTO> page = carImageService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
