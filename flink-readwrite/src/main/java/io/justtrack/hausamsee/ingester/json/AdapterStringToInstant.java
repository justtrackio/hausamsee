package io.justtrack.hausamsee.ingester.json;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class AdapterStringToInstant implements JsonSerializer<Instant>, JsonDeserializer<Instant> {
    private final DateTimeFormatter format = DateTimeFormatter.ISO_INSTANT;

    public AdapterStringToInstant() {}

    @Override
    public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
        String datetime = this.serializeToString(src);

        return new JsonPrimitive(datetime);
    }

    public String serializeToString(Instant src) {
        return this.format.format(src);
    }

    @Override
    public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        String datetime = json.getAsString();
        return this.deserializeFromString(datetime);
    }

    public Instant deserializeFromString(String data) {
        data = data.trim().replace(" ", "T");
        // if there is no timezone information, assume UTC
        if (data.length() == "0000-00-00 00-00-00".length()) {
            data = data + "Z";
        }

        DateTimeFormatter pattern = selectPatternToParse(data);

        return pattern.parse(data, Instant::from);
    }

    private static final DateTimeFormatter patternSerialize =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'.'n'Z'");
    private static final Map<Integer, DateTimeFormatter> patternSelector;

    static {
        patternSelector = Map.of(
                9, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'.'SSSSSSSSSX"),
                8, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'.'SSSSSSSSX"),
                7, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'.'SSSSSSSX"),
                6, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'.'SSSSSSX"),
                5, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'.'SSSSSX"),
                4, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'.'SSSSX"),
                3, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'.'SSSX"),
                2, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'.'SSX"),
                1, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'.'SX"),
                0, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX"));
    }

    static DateTimeFormatter selectPatternToParse(String data) {
        String[] splitData = data.split("\\.", 2);
        if (splitData.length < 2) {
            return patternSelector.get(0);
        }

        int subSecondDigits =
                (int) splitData[1].chars().takeWhile(c -> c >= '0' && c <= '9').count();

        return patternSelector.getOrDefault(subSecondDigits, patternSelector.get(9));
    }
}
