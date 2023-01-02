package io.github.redpanda4552.HifumiBot.config;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class OffsetDateTimeAdapter extends TypeAdapter<OffsetDateTime> {

    @Override
    public OffsetDateTime read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }

        try {
            String name = "";
            int year = 0, month = 0, day = 0, hour = 0, minute = 0, second = 0, nano = 0, totalSeconds = 0;

            reader.beginObject();
                reader.nextName();
                reader.beginObject();
                    reader.nextName();
                    reader.beginObject();
                        reader.nextName();
                        year = reader.nextInt();
                        reader.nextName();
                        month = reader.nextInt();
                        reader.nextName();
                        day = reader.nextInt();
                    reader.endObject();
                    reader.nextName();
                    reader.beginObject();
                        reader.nextName();
                        hour = reader.nextInt();
                        reader.nextName();
                        minute = reader.nextInt();
                        reader.nextName();
                        second = reader.nextInt();
                        reader.nextName();
                        nano = reader.nextInt();
                    reader.endObject();
                reader.endObject();
                reader.nextName();
                reader.beginObject();
                    reader.nextName();
                    totalSeconds = reader.nextInt();
                reader.endObject();
            reader.endObject();
            return OffsetDateTime.of(year, month, month, hour, minute, second, nano, ZoneOffset.ofTotalSeconds(totalSeconds));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    @Override
    public void write(JsonWriter writer, OffsetDateTime dateTime) throws IOException {
        if (dateTime == null) {
            writer.nullValue();
            return;
        }
        
        writer.beginObject();
            writer.name("dateTime");
            writer.beginObject();
                writer.name("date");
                writer.beginObject();
                    writer.name("year").value(dateTime.getYear());
                    writer.name("month").value(dateTime.getMonthValue());
                    writer.name("day").value(dateTime.getDayOfMonth());
                writer.endObject();
                writer.name("time");
                writer.beginObject();
                    writer.name("hour").value(dateTime.getHour());
                    writer.name("minute").value(dateTime.getMinute());
                    writer.name("second").value(dateTime.getSecond());
                    writer.name("nano").value(dateTime.getNano());
                writer.endObject();
            writer.endObject();
            writer.name("offset");
            writer.beginObject();
                writer.name("totalSeconds").value(dateTime.getOffset().getTotalSeconds());
            writer.endObject();
        writer.endObject();
    }
    
}
