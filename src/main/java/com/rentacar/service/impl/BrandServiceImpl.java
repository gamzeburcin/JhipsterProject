package com.rentacar.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.rentacar.domain.Brand;
import com.rentacar.repository.BrandRepository;
import com.rentacar.repository.search.BrandSearchRepository;
import com.rentacar.service.BrandService;
import com.rentacar.service.dto.BrandDTO;
import com.rentacar.service.mapper.BrandMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Brand}.
 */
@Service
@Transactional
public class BrandServiceImpl implements BrandService {

    private final Logger log = LoggerFactory.getLogger(BrandServiceImpl.class);

    private final BrandRepository brandRepository;

    private final BrandMapper brandMapper;

    private final BrandSearchRepository brandSearchRepository;

    public BrandServiceImpl(BrandRepository brandRepository, BrandMapper brandMapper, BrandSearchRepository brandSearchRepository) {
        this.brandRepository = brandRepository;
        this.brandMapper = brandMapper;
        this.brandSearchRepository = brandSearchRepository;
    }

    @Override
    public BrandDTO save(BrandDTO brandDTO) {
        log.debug("Request to save Brand : {}", brandDTO);
        Brand brand = brandMapper.toEntity(brandDTO);
        brand = brandRepository.save(brand);
        BrandDTO result = brandMapper.toDto(brand);
        brandSearchRepository.save(brand);
        return result;
    }

    @Override
    public Optional<BrandDTO> partialUpdate(BrandDTO brandDTO) {
        log.debug("Request to partially update Brand : {}", brandDTO);

        return brandRepository
            .findById(brandDTO.getId())
            .map(
                existingBrand -> {
                    brandMapper.partialUpdate(existingBrand, brandDTO);
                    return existingBrand;
                }
            )
            .map(brandRepository::save)
            .map(
                savedBrand -> {
                    brandSearchRepository.save(savedBrand);

                    return savedBrand;
                }
            )
            .map(brandMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BrandDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Brands");
        return brandRepository.findAll(pageable).map(brandMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BrandDTO> findOne(Long id) {
        log.debug("Request to get Brand : {}", id);
        return brandRepository.findById(id).map(brandMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Brand : {}", id);
        brandRepository.deleteById(id);
        brandSearchRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BrandDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Brands for query {}", query);
        return brandSearchRepository.search(queryStringQuery(query), pageable).map(brandMapper::toDto);
    }
}
