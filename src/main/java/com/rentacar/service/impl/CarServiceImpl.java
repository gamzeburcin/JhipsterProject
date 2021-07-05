package com.rentacar.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.rentacar.domain.Car;
import com.rentacar.repository.CarRepository;
import com.rentacar.repository.search.CarSearchRepository;
import com.rentacar.service.CarService;
import com.rentacar.service.dto.CarDTO;
import com.rentacar.service.mapper.CarMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Car}.
 */
@Service
@Transactional
public class CarServiceImpl implements CarService {

    private final Logger log = LoggerFactory.getLogger(CarServiceImpl.class);

    private final CarRepository carRepository;

    private final CarMapper carMapper;

    private final CarSearchRepository carSearchRepository;

    public CarServiceImpl(CarRepository carRepository, CarMapper carMapper, CarSearchRepository carSearchRepository) {
        this.carRepository = carRepository;
        this.carMapper = carMapper;
        this.carSearchRepository = carSearchRepository;
    }

    @Override
    public CarDTO save(CarDTO carDTO) {
        log.debug("Request to save Car : {}", carDTO);
        Car car = carMapper.toEntity(carDTO);
        car = carRepository.save(car);
        CarDTO result = carMapper.toDto(car);
        carSearchRepository.save(car);
        return result;
    }

    @Override
    public Optional<CarDTO> partialUpdate(CarDTO carDTO) {
        log.debug("Request to partially update Car : {}", carDTO);

        return carRepository
            .findById(carDTO.getId())
            .map(
                existingCar -> {
                    carMapper.partialUpdate(existingCar, carDTO);
                    return existingCar;
                }
            )
            .map(carRepository::save)
            .map(
                savedCar -> {
                    carSearchRepository.save(savedCar);

                    return savedCar;
                }
            )
            .map(carMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CarDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Cars");
        return carRepository.findAll(pageable).map(carMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CarDTO> findOne(Long id) {
        log.debug("Request to get Car : {}", id);
        return carRepository.findById(id).map(carMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Car : {}", id);
        carRepository.deleteById(id);
        carSearchRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CarDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Cars for query {}", query);
        return carSearchRepository.search(queryStringQuery(query), pageable).map(carMapper::toDto);
    }
}
