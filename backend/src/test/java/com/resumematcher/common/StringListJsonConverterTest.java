package com.resumematcher.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StringListJsonConverterTest {

    private final StringListJsonConverter converter = new StringListJsonConverter();

    @Test
    void convertsListToJsonAndBack() {
        List<String> input = List.of("Java", "Spring Boot", "PostgreSQL");
        String json = converter.convertToDatabaseColumn(input);

        assertEquals("[\"Java\",\"Spring Boot\",\"PostgreSQL\"]", json);
        assertEquals(input, converter.convertToEntityAttribute(json));
    }

    @Test
    void handlesNullAndBlankValuesGracefully() {
        assertEquals("[]", converter.convertToDatabaseColumn(null));
        assertTrue(converter.convertToEntityAttribute(null).isEmpty());
        assertTrue(converter.convertToEntityAttribute("   ").isEmpty());
    }
}
