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
 * Criteria class for the {@link com.rentacar.domain.Rental} entity. This class is used
 * in {@link com.rentacar.web.rest.RentalResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /rentals?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class RentalCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private ZonedDateTimeFilter rentDate;

    private ZonedDateTimeFilter returnDate;

    private LongFilter customerId;

    private LongFilter carId;

    public RentalCriteria() {}

    public RentalCriteria(RentalCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.rentDate = other.rentDate == null ? null : other.rentDate.copy();
        this.returnDate = other.returnDate == null ? null : other.returnDate.copy();
        this.customerId = other.customerId == null ? null : other.customerId.copy();
        this.carId = other.carId == null ? null : other.carId.copy();
    }

    @Override
    public RentalCriteria copy() {
        return new RentalCriteria(this);
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

    public ZonedDateTimeFilter getRentDate() {
        return rentDate;
    }

    public ZonedDateTimeFilter rentDate() {
        if (rentDate == null) {
            rentDate = new ZonedDateTimeFilter();
        }
        return rentDate;
    }

    public void setRentDate(ZonedDateTimeFilter rentDate) {
        this.rentDate = rentDate;
    }

    public ZonedDateTimeFilter getReturnDate() {
        return returnDate;
    }

    public ZonedDateTimeFilter returnDate() {
        if (returnDate == null) {
            returnDate = new ZonedDateTimeFilter();
        }
        return returnDate;
    }

    public void setReturnDate(ZonedDateTimeFilter returnDate) {
        this.returnDate = returnDate;
    }

    public LongFilter getCustomerId() {
        return customerId;
    }

    public LongFilter customerId() {
        if (customerId == null) {
            customerId = new LongFilter();
        }
        return customerId;
    }

    public void setCustomerId(LongFilter customerId) {
        this.customerId = customerId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RentalCriteria that = (RentalCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(rentDate, that.rentDate) &&
            Objects.equals(returnDate, that.returnDate) &&
            Objects.equals(customerId, that.customerId) &&
            Objects.equals(carId, that.carId)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, rentDate, returnDate, customerId, carId);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RentalCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (rentDate != null ? "rentDate=" + rentDate + ", " : "") +
            (returnDate != null ? "returnDate=" + returnDate + ", " : "") +
            (customerId != null ? "customerId=" + customerId + ", " : "") +
            (carId != null ? "carId=" + carId + ", " : "") +
            "}";
    }
}
