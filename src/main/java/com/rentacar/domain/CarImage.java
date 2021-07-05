package com.rentacar.domain;

import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * A CarImage.
 */
@Entity
@Table(name = "car_image")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "carimage")
public class CarImage implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "car_id")
    private Long carId;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "date")
    private ZonedDateTime date;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CarImage id(Long id) {
        this.id = id;
        return this;
    }

    public Long getCarId() {
        return this.carId;
    }

    public CarImage carId(Long carId) {
        this.carId = carId;
        return this;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

    public String getImagePath() {
        return this.imagePath;
    }

    public CarImage imagePath(String imagePath) {
        this.imagePath = imagePath;
        return this;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public ZonedDateTime getDate() {
        return this.date;
    }

    public CarImage date(ZonedDateTime date) {
        this.date = date;
        return this;
    }

    public void setDate(ZonedDateTime date) {
        this.date = date;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CarImage)) {
            return false;
        }
        return id != null && id.equals(((CarImage) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CarImage{" +
            "id=" + getId() +
            ", carId=" + getCarId() +
            ", imagePath='" + getImagePath() + "'" +
            ", date='" + getDate() + "'" +
            "}";
    }
}
