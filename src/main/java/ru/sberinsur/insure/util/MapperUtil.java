package ru.sberinsur.insure.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.GregorianCalendar;


@Slf4j
public final class MapperUtil {

    public static final ObjectMapper INSURE_OBJECT_MAPPER = makeInsureObjectMapper();

    public static final String EMPTY_STRING = "";

    private MapperUtil() {
        //no instance
    }

    private static ObjectMapper makeInsureObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper;
    }

    public static XMLGregorianCalendar mapLocalDate(final LocalDate localDate) throws DatatypeConfigurationException {
        if (localDate == null) {
            return null;
        }

        return DatatypeFactory.newInstance().newXMLGregorianCalendar(localDate.toString());
    }

    public static XMLGregorianCalendar mapLocalDateTime(final LocalDateTime localDateTime) throws DatatypeConfigurationException {
        if (localDateTime == null) {
            return null;
        }

        final GregorianCalendar gregorianCalendar = GregorianCalendar.from(localDateTime.atZone(ZoneId.systemDefault()));
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
    }

    public static XMLGregorianCalendar mapOffsetDateTime(OffsetDateTime offsetDateTime) throws DatatypeConfigurationException {
        return mapLocalDateTime(offsetDateTime.toLocalDateTime());
    }

    public static LocalDate mapGregorianCalendar(final XMLGregorianCalendar date) {
        if (date == null) {
            return null;
        }

        return date.toGregorianCalendar().toZonedDateTime().toLocalDate();
    }

    public static OffsetDateTime mapGregorianCalendarDateTime(final XMLGregorianCalendar date) {
        if (date == null) {
            return null;
        }

        return date.toGregorianCalendar().toZonedDateTime().toOffsetDateTime();
    }

    public static String toJsonString(Object value) {
        if (ObjectUtils.isEmpty(value)) {
            return EMPTY_STRING;
        }

        String result = EMPTY_STRING;
        try {
            result = INSURE_OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.error(MessageFormat.format("Error convert to string object {0}", value), e);
        }

        return result;
    }

    public static <T> String toXmlString(T value) {
        if (StringUtils.isEmpty(value)) {
            return EMPTY_STRING;
        }

        final StringWriter xmlWriter = new StringWriter();
        try {
            JAXBContext.newInstance(value.getClass()).createMarshaller().marshal(value, xmlWriter);
        } catch (JAXBException e) {
            log.error(MessageFormat.format("Error convert to string object {0}", value), e);
        }

        return xmlWriter.toString();
    }

    public static <T> String toString(T value, String mediaType) {
        if (StringUtils.isEmpty(value)) {
            return EMPTY_STRING;
        }

        if (MediaType.APPLICATION_XML.equals(mediaType)) {
            return toXmlString(value);
        } else {
            return toJsonString(value);
        }
    }
}
