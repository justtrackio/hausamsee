package io.justtrack.hausamsee.ingester.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.Instant;

public class DeserializationSchema<T> implements org.apache.flink.api.common.serialization.DeserializationSchema<T> {
    private static final long serialVersionUID = 1L;

    private final Class<T> clazz;
    private Gson serializer;

    public DeserializationSchema(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void open(InitializationContext context) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Instant.class, new AdapterStringToInstant());

        this.serializer = gsonBuilder.create();
    }

    @Override
    public T deserialize(byte[] message) {
        Reader reader = new InputStreamReader(new ByteArrayInputStream(message));

        return this.serializer.fromJson(reader, this.clazz);
    }

    @Override
    public boolean isEndOfStream(T nextElement) {
        return false;
    }

    @Override
    public TypeInformation<T> getProducedType() {
        return TypeInformation.of(this.clazz);
    }
}
