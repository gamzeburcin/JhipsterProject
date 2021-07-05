package com.rentacar.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.rentacar.domain.Rental;
import com.rentacar.repository.RentalRepository;
import com.rentacar.repository.search.RentalSearchRepository;
import com.rentacar.service.RentalService;
import com.rentacar.service.dto.RentalDTO;
import com.rentacar.service.mapper.RentalMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Rental}.
 */
@Service
@Transactional
public class RentalServiceImpl implements RentalService {

    private final Logger log = LoggerFactory.getLogger(RentalServiceImpl.class);

    private final RentalRepository rentalRepository;

    private final RentalMapper rentalMapper;

    private final RentalSearchRepository rentalSearchRepository;

    public RentalServiceImpl(RentalRepository rentalRepository, RentalMapper rentalMapper, RentalSearchRepository rentalSearchRepository) {
        this.rentalRepository = rentalRepository;
        this.rentalMapper = rentalMapper;
        this.rentalSearchRepository = rentalSearchRepository;
    }

    @Override
    public RentalDTO save(RentalDTO rentalDTO) {
        log.debug("Request to save Rental : {}", rentalDTO);
        Rental rental = rentalMapper.toEntity(rentalDTO);
        rental = rentalRepository.save(rental);
        RentalDTO result = rentalMapper.toDto(rental);
        rentalSearchRepository.save(rental);
        return result;
    }

    @Override
    public Optional<RentalDTO> partialUpdate(RentalDTO rentalDTO) {
        log.debug("Request to partially update Rental : {}", rentalDTO);

        return rentalRepository
            .findById(rentalDTO.getId())
            .map(
                existingRental -> {
                    rentalMapper.partialUpdate(existingRental, rentalDTO);
                    return existingRental;
                }
            )
            .map(rentalRepository::save)
            .map(
                savedRental -> {
                    rentalSearchRepository.save(savedRental);

                    return savedRental;
                }
            )
            .map(rentalMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RentalDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Rentals");
        return rentalRepository.findAll(pageable).map(rentalMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RentalDTO> findOne(Long id) {
        log.debug("Request to get Rental : {}", id);
        return rentalRepository.findById(id).map(rentalMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Rental : {}", id);
        rentalRepository.deleteById(id);
        rentalSearchRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RentalDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Rentals for query {}", query);
        return rentalSearchRepository.search(queryStringQuery(query), pageable).map(rentalMapper::toDto);
    }
}
