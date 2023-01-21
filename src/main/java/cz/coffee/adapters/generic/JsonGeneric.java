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
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright coffeeRequired nd contributors
 */
package cz.coffee.adapters.generic;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.jetbrains.annotations.NotNull;

public class JsonGeneric implements JsonGenericAdapter<Object> {
    @Override
    public @NotNull JsonElement toJson(Object object) {
        return JsonNull.INSTANCE;
    }

    @Override
    public Object fromJson(JsonElement json) {
        return null;
    }

    @Override
    public Class<?> typeOf(JsonElement json) {
        String potentialClass;
        if (json.getAsJsonObject().has(GSON_GENERIC_ADAPTER_KEY)) {
            potentialClass = json.getAsJsonObject().get(GSON_GENERIC_ADAPTER_KEY).getAsString();
        } else {
            potentialClass = json.getAsJsonObject().get(ConfigurationSerialization.SERIALIZED_TYPE_KEY).getAsString();
        }
        try {
            return Class.forName(potentialClass);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }
}
