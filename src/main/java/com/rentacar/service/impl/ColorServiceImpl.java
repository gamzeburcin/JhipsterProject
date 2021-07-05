package com.rentacar.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.rentacar.domain.Color;
import com.rentacar.repository.ColorRepository;
import com.rentacar.repository.search.ColorSearchRepository;
import com.rentacar.service.ColorService;
import com.rentacar.service.dto.ColorDTO;
import com.rentacar.service.mapper.ColorMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Color}.
 */
@Service
@Transactional
public class ColorServiceImpl implements ColorService {

    private final Logger log = LoggerFactory.getLogger(ColorServiceImpl.class);

    private final ColorRepository colorRepository;

    private final ColorMapper colorMapper;

    private final ColorSearchRepository colorSearchRepository;

    public ColorServiceImpl(ColorRepository colorRepository, ColorMapper colorMapper, ColorSearchRepository colorSearchRepository) {
        this.colorRepository = colorRepository;
        this.colorMapper = colorMapper;
        this.colorSearchRepository = colorSearchRepository;
    }

    @Override
    public ColorDTO save(ColorDTO colorDTO) {
        log.debug("Request to save Color : {}", colorDTO);
        Color color = colorMapper.toEntity(colorDTO);
        color = colorRepository.save(color);
        ColorDTO result = colorMapper.toDto(color);
        colorSearchRepository.save(color);
        return result;
    }

    @Override
    public Optional<ColorDTO> partialUpdate(ColorDTO colorDTO) {
        log.debug("Request to partially update Color : {}", colorDTO);

        return colorRepository
            .findById(colorDTO.getId())
            .map(
                existingColor -> {
                    colorMapper.partialUpdate(existingColor, colorDTO);
                    return existingColor;
                }
            )
            .map(colorRepository::save)
            .map(
                savedColor -> {
                    colorSearchRepository.save(savedColor);

                    return savedColor;
                }
            )
            .map(colorMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ColorDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Colors");
        return colorRepository.findAll(pageable).map(colorMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ColorDTO> findOne(Long id) {
        log.debug("Request to get Color : {}", id);
        return colorRepository.findById(id).map(colorMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Color : {}", id);
        colorRepository.deleteById(id);
        colorSearchRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ColorDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Colors for query {}", query);
        return colorSearchRepository.search(queryStringQuery(query), pageable).map(colorMapper::toDto);
    }
}
