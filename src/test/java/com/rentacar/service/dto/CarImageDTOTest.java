package com.rentacar.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.rentacar.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CarImageDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CarImageDTO.class);
        CarImageDTO carImageDTO1 = new CarImageDTO();
        carImageDTO1.setId(1L);
        CarImageDTO carImageDTO2 = new CarImageDTO();
        assertThat(carImageDTO1).isNotEqualTo(carImageDTO2);
        carImageDTO2.setId(carImageDTO1.getId());
        assertThat(carImageDTO1).isEqualTo(carImageDTO2);
        carImageDTO2.setId(2L);
        assertThat(carImageDTO1).isNotEqualTo(carImageDTO2);
        carImageDTO1.setId(null);
        assertThat(carImageDTO1).isNotEqualTo(carImageDTO2);
    }
}
