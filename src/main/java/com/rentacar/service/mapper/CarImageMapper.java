package com.rentacar.service.mapper;

import com.rentacar.domain.*;
import com.rentacar.service.dto.CarImageDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link CarImage} and its DTO {@link CarImageDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface CarImageMapper extends EntityMapper<CarImageDTO, CarImage> {}
