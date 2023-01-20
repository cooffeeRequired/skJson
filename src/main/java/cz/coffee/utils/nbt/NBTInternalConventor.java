/**
 *   This file is part of skJson.
 * <p>
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * <p>
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * <p>
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright coffeeRequired nd contributors
 */
package cz.coffee.utils.nbt;

import com.google.gson.JsonElement;
import com.shanebeestudios.skbee.api.NBT.NBTCompound;
import com.shanebeestudios.skbee.api.NBT.NBTContainer;


public class NBTInternalConventor {

    private final Object nbt;

    public NBTInternalConventor(Object nbt) {
        this.nbt = nbt;
    }

    public static String nbtToJson(NBTCompound container) {
        return container.toString();
    }

    public static NBTCompound toNBT(JsonElement json) {
        String nbtString = json.getAsJsonObject().get("nbt").getAsString();
        return new NBTContainer(nbtString);
    }

    public NBTCompound getCompound() {
        return new NBTContainer(nbt.toString());
    }

}
