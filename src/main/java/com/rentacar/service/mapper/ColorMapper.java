package com.rentacar.service.mapper;

import com.rentacar.domain.*;
import com.rentacar.service.dto.ColorDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Color} and its DTO {@link ColorDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface ColorMapper extends EntityMapper<ColorDTO, Color> {}
