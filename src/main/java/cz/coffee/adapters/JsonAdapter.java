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
package cz.coffee.adapters;

import ch.njol.yggdrasil.YggdrasilSerializable;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.shanebeestudios.skbee.api.NBT.*;
import cz.coffee.adapters.generic.*;
import cz.coffee.utils.ErrorHandler;
import cz.coffee.utils.nbt.JsonNBT;
import cz.coffee.utils.nbt.NBTInternalConvertor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static cz.coffee.utils.ErrorHandler.sendMessage;
import static cz.coffee.utils.SimpleUtil.gsonAdapter;
import static cz.coffee.utils.SimpleUtil.printPrettyStackTrace;
import static cz.coffee.utils.config.Config._NBT_SUPPORTED;

public class JsonAdapter {

    public static JsonElement toJson(Object input) {
        if (input != null) {
            boolean isSerializable = (input instanceof YggdrasilSerializable || input instanceof ConfigurationSerializable);
            boolean isNBT = false;
            if (_NBT_SUPPORTED) {
                isNBT = (input instanceof NBTCustomEntity || input instanceof NBTCustomBlock || input instanceof NBTCustomSlot || input instanceof NBTCustomItemType || input instanceof NBTCustomTileEntity);
            }
            if (input instanceof World) {
                return new JsonWorld().toJson((World) input);
            } else if (input instanceof ItemStack) {
                return new JsonItemStack().toJson((ItemStack) input);
            } else if (input instanceof Inventory) {
                return new JsonInventory().toJson((Inventory) input);
            } else if (input instanceof Chunk) {
                return new JsonChunk().toJson((Chunk) input);
            } else if (isNBT) {
                NBTCompound nbt = new NBTInternalConvertor(input).getCompound();
                return new JsonNBT().toJson(nbt);
            } else if (input instanceof Entity) {
                return new JsonEntity().toJson((Entity) input);
            }
            if (isSerializable) {
                return gsonAdapter.toJsonTree(input);
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
            return NBTInternalConvertor.toNBT(json.getAsJsonObject().get("nbt"));
        } else if (Chunk.class.isAssignableFrom(clazz)) {
            return new JsonChunk().fromJson(json);
        } else if (Entity.class.isAssignableFrom(clazz)) {
            return new JsonEntity().fromJson(json);
        } else if (ItemStack.class.isAssignableFrom(clazz)){
            return new JsonItemStack().fromJson(json);
        } else {
            try {
                return gsonAdapter.fromJson(json, ConfigurationSerializable.class);
            } catch (Exception ex) {
                printPrettyStackTrace(ex, 5);
            }
        }
//        } else {
//            Object returnData = SimpleUtil.gsonAdapter.fromJson(json, ConfigurationSerializable.class);
//            if (returnData instanceof ItemStack) {
//                JsonItemStack jsonItem = new JsonItemStack((ItemStack) returnData);
//                jsonItem.setOthers(json);
//                return jsonItem.getItemStack();
//            }
//            return returnData;
//        }
        return null;
    }
}
