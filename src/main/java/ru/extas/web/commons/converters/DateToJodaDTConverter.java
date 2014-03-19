package ru.extas.web.commons.converters;

import com.vaadin.data.util.converter.Converter;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Locale;

@Component
/**
 * <p>DateToJodaDTConverter class.</p>
 *
 * @author Valery_2
 * @version $Id: $Id
 */
public class DateToJodaDTConverter implements Converter<Date, DateTime> {
    private static final long serialVersionUID = 6275343643194858906L;

    /*
     * (non-Javadoc)
     *
     * @see com.vaadin.data.util.converter.Converter#getModelType()
     */
    /** {@inheritDoc} */
    @Override
    public Class<DateTime> getModelType() {
        return DateTime.class;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.vaadin.data.util.converter.Converter#convertToModel(java.lang.Object,
     * java.util.Locale)
     */
    /** {@inheritDoc} */
    @Override
    public Class<Date> getPresentationType() {
        return Date.class;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.vaadin.data.util.converter.Converter#convertToModel(java.lang.Object,
     * java.lang.Class, java.util.Locale)
     */
    /** {@inheritDoc} */
    @Override
    public DateTime convertToModel(final Date value, final Class<? extends DateTime> targetType, final Locale locale)
            throws ConversionException {
        if (value == null)
            return null;
        return new DateTime(value);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.vaadin.data.util.converter.Converter#convertToPresentation(java.lang
     * .Object, java.lang.Class, java.util.Locale)
     */
    /** {@inheritDoc} */
    @Override
    public Date convertToPresentation(final DateTime value, final Class<? extends Date> targetType, final Locale locale)
            throws ConversionException {
        if (value == null)
            return null;
        return value.toDate();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.vaadin.data.util.converter.Converter#convertToModel(java.lang.Object,
     * java.util.Locale)
     */
    /**
     * <p>convertToModel.</p>
     *
     * @param value a {@link java.util.Date} object.
     * @param locale a {@link java.util.Locale} object.
     * @return a {@link org.joda.time.DateTime} object.
     * @throws com.vaadin.data.util.converter.Converter$ConversionException if any.
     */
    public DateTime convertToModel(final Date value, final Locale locale)
            throws com.vaadin.data.util.converter.Converter.ConversionException {
        return convertToModel(value, null, locale);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.vaadin.data.util.converter.Converter#convertToPresentation(java.lang
     * .Object, java.util.Locale)
     */
    /**
     * <p>convertToPresentation.</p>
     *
     * @param value a {@link org.joda.time.DateTime} object.
     * @param locale a {@link java.util.Locale} object.
     * @return a {@link java.util.Date} object.
     * @throws com.vaadin.data.util.converter.Converter$ConversionException if any.
     */
    public Date convertToPresentation(final DateTime value, final Locale locale)
            throws com.vaadin.data.util.converter.Converter.ConversionException {
        return convertToPresentation(value, null, locale);
    }
}
