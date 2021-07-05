package com.rentacar.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.BooleanFilter;
import tech.jhipster.service.filter.DoubleFilter;
import tech.jhipster.service.filter.Filter;
import tech.jhipster.service.filter.FloatFilter;
import tech.jhipster.service.filter.IntegerFilter;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;

/**
 * Criteria class for the {@link com.rentacar.domain.Car} entity. This class is used
 * in {@link com.rentacar.web.rest.CarResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /cars?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class CarCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private LongFilter brandId;

    private LongFilter colorId;

    private StringFilter modelYear;

    private DoubleFilter dailyPrice;

    private StringFilter description;

    public CarCriteria() {}

    public CarCriteria(CarCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.brandId = other.brandId == null ? null : other.brandId.copy();
        this.colorId = other.colorId == null ? null : other.colorId.copy();
        this.modelYear = other.modelYear == null ? null : other.modelYear.copy();
        this.dailyPrice = other.dailyPrice == null ? null : other.dailyPrice.copy();
        this.description = other.description == null ? null : other.description.copy();
    }

    @Override
    public CarCriteria copy() {
        return new CarCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public LongFilter id() {
        if (id == null) {
            id = new LongFilter();
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public LongFilter getBrandId() {
        return brandId;
    }

    public LongFilter brandId() {
        if (brandId == null) {
            brandId = new LongFilter();
        }
        return brandId;
    }

    public void setBrandId(LongFilter brandId) {
        this.brandId = brandId;
    }

    public LongFilter getColorId() {
        return colorId;
    }

    public LongFilter colorId() {
        if (colorId == null) {
            colorId = new LongFilter();
        }
        return colorId;
    }

    public void setColorId(LongFilter colorId) {
        this.colorId = colorId;
    }

    public StringFilter getModelYear() {
        return modelYear;
    }

    public StringFilter modelYear() {
        if (modelYear == null) {
            modelYear = new StringFilter();
        }
        return modelYear;
    }

    public void setModelYear(StringFilter modelYear) {
        this.modelYear = modelYear;
    }

    public DoubleFilter getDailyPrice() {
        return dailyPrice;
    }

    public DoubleFilter dailyPrice() {
        if (dailyPrice == null) {
            dailyPrice = new DoubleFilter();
        }
        return dailyPrice;
    }

    public void setDailyPrice(DoubleFilter dailyPrice) {
        this.dailyPrice = dailyPrice;
    }

    public StringFilter getDescription() {
        return description;
    }

    public StringFilter description() {
        if (description == null) {
            description = new StringFilter();
        }
        return description;
    }

    public void setDescription(StringFilter description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CarCriteria that = (CarCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(brandId, that.brandId) &&
            Objects.equals(colorId, that.colorId) &&
            Objects.equals(modelYear, that.modelYear) &&
            Objects.equals(dailyPrice, that.dailyPrice) &&
            Objects.equals(description, that.description)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, brandId, colorId, modelYear, dailyPrice, description);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CarCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (brandId != null ? "brandId=" + brandId + ", " : "") +
            (colorId != null ? "colorId=" + colorId + ", " : "") +
            (modelYear != null ? "modelYear=" + modelYear + ", " : "") +
            (dailyPrice != null ? "dailyPrice=" + dailyPrice + ", " : "") +
            (description != null ? "description=" + description + ", " : "") +
            "}";
    }
}
