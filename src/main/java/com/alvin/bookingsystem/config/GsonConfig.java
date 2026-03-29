package com.alvin.bookingsystem.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Shared {@link Gson} for cache materialization and other non-HTTP serialization.
 * {@link LocalDateTime} supports ISO strings and JSON arrays (as produced when Redis values are read as maps).
 */
@Configuration
public class GsonConfig {

    @Bean
    public Gson gson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .create();
    }

    private static final class LocalDateTimeTypeAdapter extends TypeAdapter<LocalDateTime> {

        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.toString());
            }
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            if (in.peek() == JsonToken.STRING) {
                return LocalDateTime.parse(in.nextString());
            }
            if (in.peek() == JsonToken.BEGIN_ARRAY) {
                in.beginArray();
                int year = in.nextInt();
                int month = in.nextInt();
                int day = in.nextInt();
                int hour = in.nextInt();
                int minute = in.nextInt();
                int second = in.nextInt();
                int nano = in.hasNext() ? in.nextInt() : 0;
                in.endArray();
                return LocalDateTime.of(year, month, day, hour, minute, second, nano);
            }
            throw new IOException("Cannot deserialize LocalDateTime from " + in.peek());
        }
    }
}
