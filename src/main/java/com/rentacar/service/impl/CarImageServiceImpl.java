package com.rentacar.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.rentacar.domain.CarImage;
import com.rentacar.repository.CarImageRepository;
import com.rentacar.repository.search.CarImageSearchRepository;
import com.rentacar.service.CarImageService;
import com.rentacar.service.dto.CarImageDTO;
import com.rentacar.service.mapper.CarImageMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link CarImage}.
 */
@Service
@Transactional
public class CarImageServiceImpl implements CarImageService {

    private final Logger log = LoggerFactory.getLogger(CarImageServiceImpl.class);

    private final CarImageRepository carImageRepository;

    private final CarImageMapper carImageMapper;

    private final CarImageSearchRepository carImageSearchRepository;

    public CarImageServiceImpl(
        CarImageRepository carImageRepository,
        CarImageMapper carImageMapper,
        CarImageSearchRepository carImageSearchRepository
    ) {
        this.carImageRepository = carImageRepository;
        this.carImageMapper = carImageMapper;
        this.carImageSearchRepository = carImageSearchRepository;
    }

    @Override
    public CarImageDTO save(CarImageDTO carImageDTO) {
        log.debug("Request to save CarImage : {}", carImageDTO);
        CarImage carImage = carImageMapper.toEntity(carImageDTO);
        carImage = carImageRepository.save(carImage);
        CarImageDTO result = carImageMapper.toDto(carImage);
        carImageSearchRepository.save(carImage);
        return result;
    }

    @Override
    public Optional<CarImageDTO> partialUpdate(CarImageDTO carImageDTO) {
        log.debug("Request to partially update CarImage : {}", carImageDTO);

        return carImageRepository
            .findById(carImageDTO.getId())
            .map(
                existingCarImage -> {
                    carImageMapper.partialUpdate(existingCarImage, carImageDTO);
                    return existingCarImage;
                }
            )
            .map(carImageRepository::save)
            .map(
                savedCarImage -> {
                    carImageSearchRepository.save(savedCarImage);

                    return savedCarImage;
                }
            )
            .map(carImageMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CarImageDTO> findAll(Pageable pageable) {
        log.debug("Request to get all CarImages");
        return carImageRepository.findAll(pageable).map(carImageMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CarImageDTO> findOne(Long id) {
        log.debug("Request to get CarImage : {}", id);
        return carImageRepository.findById(id).map(carImageMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete CarImage : {}", id);
        carImageRepository.deleteById(id);
        carImageSearchRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CarImageDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of CarImages for query {}", query);
        return carImageSearchRepository.search(queryStringQuery(query), pageable).map(carImageMapper::toDto);
    }
}
