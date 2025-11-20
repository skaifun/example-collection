package org.examples.restful.converter;

import jakarta.persistence.Converter;
import jakarta.persistence.AttributeConverter;

import java.time.Instant;

@Converter(autoApply = true)
public class InstantConverter implements AttributeConverter<Instant, Long> {

    @Override
    public Long convertToDatabaseColumn(Instant instant) {
        return instant == null ? null : instant.toEpochMilli();
    }

    @Override
    public Instant convertToEntityAttribute(Long timestamp) {
        return timestamp == null ? null : Instant.ofEpochMilli(timestamp);
    }
}
