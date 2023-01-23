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
package cz.coffee.utils.nbt;

import ch.njol.skript.doc.Since;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.shanebeestudios.skbee.api.NBT.NBTCompound;
import cz.coffee.adapters.generic.JsonGenericAdapter;
import cz.coffee.utils.Type;
import org.jetbrains.annotations.NotNull;

import static cz.coffee.utils.json.JsonUtils.check;

@Since("2.5")
public class JsonNBT implements JsonGenericAdapter<NBTCompound> {
    @Override
    public @NotNull JsonElement toJson(NBTCompound object) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("??", object.getClass().getName());
        jsonObject.addProperty("nbt", NBTInternalConvertor.nbtToJson(object));
        return jsonObject;
    }

    @Override
    public NBTCompound fromJson(JsonElement json) {
        return NBTInternalConvertor.toNBT(json);
    }

    @Override
    public Class<NBTCompound> typeOf(JsonElement json) {
        if (check(json, NBTCompound.class.getSimpleName(), Type.KEY)) {
            return NBTCompound.class;
        }
        return null;
    }
}
