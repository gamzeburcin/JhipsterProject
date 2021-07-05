package com.rentacar.service.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A DTO for the {@link com.rentacar.domain.CarImage} entity.
 */
public class CarImageDTO implements Serializable {

    private Long id;

    private Long carId;

    private String imagePath;

    private ZonedDateTime date;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public void setDate(ZonedDateTime date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CarImageDTO)) {
            return false;
        }

        CarImageDTO carImageDTO = (CarImageDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, carImageDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CarImageDTO{" +
            "id=" + getId() +
            ", carId=" + getCarId() +
            ", imagePath='" + getImagePath() + "'" +
            ", date='" + getDate() + "'" +
            "}";
    }
}
