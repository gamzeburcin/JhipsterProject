package com.rentacar.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ColorMapperTest {

    private ColorMapper colorMapper;

    @BeforeEach
    public void setUp() {
        colorMapper = new ColorMapperImpl();
    }
}
