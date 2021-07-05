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
 * Criteria class for the {@link com.rentacar.domain.Color} entity. This class is used
 * in {@link com.rentacar.web.rest.ColorResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /colors?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class ColorCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter colorName;

    public ColorCriteria() {}

    public ColorCriteria(ColorCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.colorName = other.colorName == null ? null : other.colorName.copy();
    }

    @Override
    public ColorCriteria copy() {
        return new ColorCriteria(this);
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

    public StringFilter getColorName() {
        return colorName;
    }

    public StringFilter colorName() {
        if (colorName == null) {
            colorName = new StringFilter();
        }
        return colorName;
    }

    public void setColorName(StringFilter colorName) {
        this.colorName = colorName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ColorCriteria that = (ColorCriteria) o;
        return Objects.equals(id, that.id) && Objects.equals(colorName, that.colorName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, colorName);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ColorCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (colorName != null ? "colorName=" + colorName + ", " : "") +
            "}";
    }
}
