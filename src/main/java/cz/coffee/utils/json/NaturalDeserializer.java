package cz.coffee.utils.json;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * This file is part of skJson.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <<a href="http://www.gnu.org/licenses/">...</a>>.
 * <p>
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: Wednesday (3/8/2023)
 */
public class NaturalDeserializer implements JsonDeserializer<Object> {
    public Object deserialize(JsonElement json, Type typeOfT,
                              JsonDeserializationContext context) {
        if(json.isJsonNull()) return null;
        else if(json.isJsonPrimitive()) return handlePrimitive(json.getAsJsonPrimitive());
        else if(json.isJsonArray()) return handleArray(json.getAsJsonArray(), context);
        else return handleObject(json.getAsJsonObject(), context);
    }
    private Object handlePrimitive(JsonPrimitive json) {
        if(json.isBoolean())
            return json.getAsBoolean();
        else if(json.isString())
            return json.getAsString();
        else {
            BigDecimal bigDec = json.getAsBigDecimal();
            // Find out if it is an int type
            try {
                bigDec.toBigIntegerExact();
                try { return bigDec.intValueExact(); }
                catch(ArithmeticException e) {}
                return bigDec.longValue();
            } catch(ArithmeticException e) {}
            // Just return it as a double
            return bigDec.doubleValue();
        }
    }
    private Object handleArray(JsonArray json, JsonDeserializationContext context) {
        Object[] array = new Object[json.size()];
        for(int i = 0; i < array.length; i++)
            array[i] = context.deserialize(json.get(i), Object.class);
        return array;
    }
    private Object handleObject(JsonObject json, JsonDeserializationContext context) {
        Map<String, Object> map = new HashMap<String, Object>();
        for(Map.Entry<String, JsonElement> entry : json.entrySet())
            map.put(entry.getKey(), context.deserialize(entry.getValue(), Object.class));
        return map;
    }
}