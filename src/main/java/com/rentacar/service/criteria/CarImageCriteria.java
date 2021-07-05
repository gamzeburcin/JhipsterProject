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
import tech.jhipster.service.filter.ZonedDateTimeFilter;

/**
 * Criteria class for the {@link com.rentacar.domain.CarImage} entity. This class is used
 * in {@link com.rentacar.web.rest.CarImageResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /car-images?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class CarImageCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private LongFilter carId;

    private StringFilter imagePath;

    private ZonedDateTimeFilter date;

    public CarImageCriteria() {}

    public CarImageCriteria(CarImageCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.carId = other.carId == null ? null : other.carId.copy();
        this.imagePath = other.imagePath == null ? null : other.imagePath.copy();
        this.date = other.date == null ? null : other.date.copy();
    }

    @Override
    public CarImageCriteria copy() {
        return new CarImageCriteria(this);
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

    public LongFilter getCarId() {
        return carId;
    }

    public LongFilter carId() {
        if (carId == null) {
            carId = new LongFilter();
        }
        return carId;
    }

    public void setCarId(LongFilter carId) {
        this.carId = carId;
    }

    public StringFilter getImagePath() {
        return imagePath;
    }

    public StringFilter imagePath() {
        if (imagePath == null) {
            imagePath = new StringFilter();
        }
        return imagePath;
    }

    public void setImagePath(StringFilter imagePath) {
        this.imagePath = imagePath;
    }

    public ZonedDateTimeFilter getDate() {
        return date;
    }

    public ZonedDateTimeFilter date() {
        if (date == null) {
            date = new ZonedDateTimeFilter();
        }
        return date;
    }

    public void setDate(ZonedDateTimeFilter date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CarImageCriteria that = (CarImageCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(carId, that.carId) &&
            Objects.equals(imagePath, that.imagePath) &&
            Objects.equals(date, that.date)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, carId, imagePath, date);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CarImageCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (carId != null ? "carId=" + carId + ", " : "") +
            (imagePath != null ? "imagePath=" + imagePath + ", " : "") +
            (date != null ? "date=" + date + ", " : "") +
            "}";
    }
}
