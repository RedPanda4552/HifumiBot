package io.github.redpanda4552.HifumiBot.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

    @Override
    public LocalDateTime read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }

        try {
            return LocalDateTime.parse(reader.nextString());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    @Override
    public void write(JsonWriter writer, LocalDateTime dateTime) throws IOException {
        if (dateTime == null) {
            writer.nullValue();
            return;
        }
        
        writer.value(dateTime.toString());
    }
    
}
