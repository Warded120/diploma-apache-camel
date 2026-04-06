package com.ivan.inbound.testutil;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

public class TestObjectMapperUtil {
    public static final ObjectMapper mapper = createObjectMapper();

    @SneakyThrows
    public static String mapToJson(Object object) {
        return mapper.writeValueAsString(object);
    }

    public static ObjectMapper createObjectMapper() {
        return new ObjectMapper();
    }
}
