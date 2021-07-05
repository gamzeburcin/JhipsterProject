package com.rentacar.domain;

import java.io.Serializable;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * A Car.
 */
@Entity
@Table(name = "car")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "car")
public class Car implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "brand_id")
    private Long brandId;

    @Column(name = "color_id")
    private Long colorId;

    @Column(name = "model_year")
    private String modelYear;

    @Column(name = "daily_price")
    private Double dailyPrice;

    @Column(name = "description")
    private String description;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Car id(Long id) {
        this.id = id;
        return this;
    }

    public Long getBrandId() {
        return this.brandId;
    }

    public Car brandId(Long brandId) {
        this.brandId = brandId;
        return this;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public Long getColorId() {
        return this.colorId;
    }

    public Car colorId(Long colorId) {
        this.colorId = colorId;
        return this;
    }

    public void setColorId(Long colorId) {
        this.colorId = colorId;
    }

    public String getModelYear() {
        return this.modelYear;
    }

    public Car modelYear(String modelYear) {
        this.modelYear = modelYear;
        return this;
    }

    public void setModelYear(String modelYear) {
        this.modelYear = modelYear;
    }

    public Double getDailyPrice() {
        return this.dailyPrice;
    }

    public Car dailyPrice(Double dailyPrice) {
        this.dailyPrice = dailyPrice;
        return this;
    }

    public void setDailyPrice(Double dailyPrice) {
        this.dailyPrice = dailyPrice;
    }

    public String getDescription() {
        return this.description;
    }

    public Car description(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Car)) {
            return false;
        }
        return id != null && id.equals(((Car) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Car{" +
            "id=" + getId() +
            ", brandId=" + getBrandId() +
            ", colorId=" + getColorId() +
            ", modelYear='" + getModelYear() + "'" +
            ", dailyPrice=" + getDailyPrice() +
            ", description='" + getDescription() + "'" +
            "}";
    }
}
