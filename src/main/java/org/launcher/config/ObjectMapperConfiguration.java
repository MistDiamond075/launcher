package org.launcher.config;

import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

public class ObjectMapperConfiguration {
    private final ObjectMapper mapper;

    public ObjectMapperConfiguration(){
        mapper= JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
                .enable(JsonReadFeature.ALLOW_YAML_COMMENTS)
                .propertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
                .findAndAddModules()
                .build();
    }

    public ObjectMapper getMapper(){
        return mapper;
    }
}
