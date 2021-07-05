package com.rentacar.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.rentacar.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CarImageTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CarImage.class);
        CarImage carImage1 = new CarImage();
        carImage1.setId(1L);
        CarImage carImage2 = new CarImage();
        carImage2.setId(carImage1.getId());
        assertThat(carImage1).isEqualTo(carImage2);
        carImage2.setId(2L);
        assertThat(carImage1).isNotEqualTo(carImage2);
        carImage1.setId(null);
        assertThat(carImage1).isNotEqualTo(carImage2);
    }
}
