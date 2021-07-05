package com.rentacar.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.rentacar.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ColorTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Color.class);
        Color color1 = new Color();
        color1.setId(1L);
        Color color2 = new Color();
        color2.setId(color1.getId());
        assertThat(color1).isEqualTo(color2);
        color2.setId(2L);
        assertThat(color1).isNotEqualTo(color2);
        color1.setId(null);
        assertThat(color1).isNotEqualTo(color2);
    }
}
