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
package cz.coffee.adapters;

import ch.njol.yggdrasil.YggdrasilSerializable;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.shanebeestudios.skbee.api.NBT.*;
import cz.coffee.adapters.generic.*;
import cz.coffee.utils.ErrorHandler;
import cz.coffee.utils.SimpleUtil;
import cz.coffee.utils.nbt.JsonNBT;
import cz.coffee.utils.nbt.NBTInternalConventor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static cz.coffee.utils.ErrorHandler.sendMessage;

public class JsonAdapter {

    public static JsonElement toJson(Object input) {
        if (input != null) {
            boolean isSerializable = (input instanceof YggdrasilSerializable || input instanceof ConfigurationSerializable);
            boolean isNBT = (input instanceof NBTCustomEntity || input instanceof NBTCustomBlock || input instanceof NBTCustomSlot || input instanceof NBTCustomItemType || input instanceof NBTCustomTileEntity);
            if (isSerializable)
                return SimpleUtil.gsonAdapter.toJsonTree(input);
            else {
                if (input instanceof World) {
                    World world =  (World) input;
                    return new JsonWorld().toJson(world);
                } else if (input instanceof Inventory) {
                    Inventory inventory = (Inventory) input;
                    return new JsonInventory().toJson(inventory);
                } else if (input instanceof Chunk) {
                    Chunk chunk = (Chunk) input;
                    return new JsonChunk().toJson(chunk);
                } else if (isNBT) {
                    NBTCompound nbt = new NBTInternalConventor(input).getCompound();
                    return new JsonNBT().toJson(nbt);
                } else if (input instanceof Entity) {
                    Entity entity = (Entity) input;
                    return new JsonEntity().toJson(entity);
                }
            }
        }
        return null;
    }
    public static Object fromJson(JsonElement json) {
        if (json instanceof JsonNull || json == null) return null;
        Class<?> clazz;
        try {
            clazz = new JsonGeneric().typeOf(json);
            if (clazz == null) {
                sendMessage("This type is not supported", ErrorHandler.Level.WARNING);
                return null;
            }
        } catch (NullPointerException nullP) {
            sendMessage("This type is not supported", ErrorHandler.Level.WARNING);
            return null;
        }

        if (Inventory.class.isAssignableFrom(clazz)) {
            return new JsonInventory().fromJson(json);
        } else if (World.class.isAssignableFrom(clazz)) {
            return new JsonWorld().fromJson(json);
        } else if (NBTContainer.class.isAssignableFrom(clazz)) {
            return NBTInternalConventor.toNBT(json.getAsJsonObject().get("nbt"));
        } else if (Chunk.class.isAssignableFrom(clazz)) {
            return new JsonChunk().fromJson(json);
        } else if (Entity.class.isAssignableFrom(clazz)) {
            return new JsonEntity().fromJson(json);
        } else {
            Object returnData = SimpleUtil.gsonAdapter.fromJson(json, ConfigurationSerializable.class);
            if (returnData instanceof ItemStack) {
                ItemStack itemStack = (ItemStack) returnData;
                JsonItemStack jsonItem = new JsonItemStack(itemStack);
                jsonItem.setOthers(json);
                return jsonItem.getItemStack();
            }
            return returnData;
        }
    }
}
