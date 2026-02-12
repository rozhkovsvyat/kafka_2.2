package com.work.utils.serdes;

import java.io.IOException;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

import com.fasterxml.jackson.databind.ObjectMapper;

/**Универсальный сериализатор/десериализатор объектов в формат JSON.*/
public class JsonSerde {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static <TModel> Serde<TModel> generic(Class<? extends TModel> model) {
        Serializer<TModel> serializer = (topic, data) -> {
            try {
                return OBJECT_MAPPER.writeValueAsBytes(data);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        };

        Deserializer<TModel> deserializer = (topic, data) -> {
            // Обработка пустых сообщений (tombstones), критично для корректной работы KTable.
            if (data == null) return null;
            try {
                return OBJECT_MAPPER.readValue(data, model);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        };

        return Serdes.serdeFrom(serializer, deserializer);
    }
}
