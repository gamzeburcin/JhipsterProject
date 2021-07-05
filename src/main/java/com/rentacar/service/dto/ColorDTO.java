package com.rentacar.service.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.rentacar.domain.Color} entity.
 */
public class ColorDTO implements Serializable {

    private Long id;

    private String colorName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ColorDTO)) {
            return false;
        }

        ColorDTO colorDTO = (ColorDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, colorDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ColorDTO{" +
            "id=" + getId() +
            ", colorName='" + getColorName() + "'" +
            "}";
    }
}
