package com.valor.mercury.dispatcher.serializer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.valor.mercury.common.model.WrapperEntity;
import com.valor.mercury.common.util.JsonUtils;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class WrapperEntityDeserializer implements Deserializer<WrapperEntity> {

    private static TypeReference typeReference = new TypeReference<WrapperEntity>() {};

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public WrapperEntity deserialize(String topic, byte[] data) {
        WrapperEntity wrapperEntity = null;
        try {
            wrapperEntity = JsonUtils.objectMapper.readValue(data, typeReference);
        } catch (Exception e) {

        }
        return wrapperEntity;
    }

    @Override
    public void close() {

    }
}
