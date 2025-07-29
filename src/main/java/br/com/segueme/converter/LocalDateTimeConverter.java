package br.com.segueme.converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("localDateTimeConverter")
public class LocalDateTimeConverter implements Converter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Erro ao converter a data/hora: " + value, e);
        }
    }

    @Override
public String getAsString(FacesContext context, UIComponent component, Object value) {
    if (value == null) {
        return "";
    }
    if (value instanceof LocalDateTime) {
        return ((LocalDateTime) value).format(FORMATTER);
    } else if (value instanceof java.util.Date) {
        // Converter java.util.Date para LocalDateTime
        LocalDateTime localDateTime = ((java.util.Date) value).toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime();
        return localDateTime.format(FORMATTER);
    } else {
        throw new IllegalArgumentException("O valor não é do tipo LocalDateTime ou Date: " + value);
    }
}
}