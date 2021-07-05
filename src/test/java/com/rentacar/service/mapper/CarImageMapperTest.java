package com.rentacar.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CarImageMapperTest {

    private CarImageMapper carImageMapper;

    @BeforeEach
    public void setUp() {
        carImageMapper = new CarImageMapperImpl();
    }
}
