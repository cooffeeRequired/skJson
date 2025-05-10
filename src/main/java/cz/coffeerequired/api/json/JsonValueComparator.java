package cz.coffeerequired.api.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Comparator;
import java.util.Map;

public class JsonValueComparator {

    public static Comparator<Map.Entry<String, JsonElement>> byValueAscending() {
        return (e1, e2) -> compareValues(e1.getValue(), e2.getValue(), true);
    }

    public static Comparator<Map.Entry<String, JsonElement>> byValueDescending() {
        return (e1, e2) -> compareValues(e1.getValue(), e2.getValue(), false);
    }

    private static int compareValues(JsonElement a, JsonElement b, boolean ascending) {
        boolean aPrimitive = a.isJsonPrimitive();
        boolean bPrimitive = b.isJsonPrimitive();

        // Primitive vs non-primitive
        if (aPrimitive && !bPrimitive) return -1;
        if (!aPrimitive && bPrimitive) return 1;
        if (!aPrimitive) return 0; // both non-sortable => equal

        // Both primitives
        JsonPrimitive pa = a.getAsJsonPrimitive();
        JsonPrimitive pb = b.getAsJsonPrimitive();

        int result;
        if (pa.isNumber() && pb.isNumber()) {
            result = Double.compare(pa.getAsDouble(), pb.getAsDouble());
        } else {
            result = pa.getAsString().compareTo(pb.getAsString());
        }

        return ascending ? result : -result;
    }
}

