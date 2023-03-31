package by.afinny.deposit.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.List;

@Component
public class MappingUtils {

    @Autowired
    private ObjectMapper objectMapper;

    public <T> T getObjectFromJson(String json, Class<T> objectClass) throws IOException {
        return objectMapper
                .readValue(json, objectClass);
    }

    public <T> List<T> getObjectListFromJson(String json, Class<T> objectClass) throws IOException {
        return objectMapper.readValue(json, objectMapper
                .getTypeFactory()
                .constructCollectionType(List.class, objectClass));
    }

    public String asJsonString(final Object obj) throws JsonProcessingException {
        return objectMapper
                .writeValueAsString(obj);
    }
}
