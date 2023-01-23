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

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.util.slot.Slot;
import com.google.gson.JsonElement;
import cz.coffee.adapters.JsonAdapter;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static cz.coffee.utils.json.JsonUtils.fromString2JsonElement;
import static cz.coffee.utils.json.JsonUtils.isClassicType;


/**
 * <p>
 * The class represent a serializer/deserializer for another object than @ConfigurationSerializable
 * the {@link JsonGenericAdapter} interface javadocs.
 */
@SuppressWarnings("unused")

public interface JsonGenericAdapter<T> {

    @SuppressWarnings("unchecked")
    static ItemStack parseItem(Object skriptItem, Expression<?> expression, Event e) {
        if (skriptItem instanceof ItemStack) {
            Expression<?> expr = expression.getConvertedExpression(ItemStack.class);
            if (expr != null) {
                return (ItemStack) expr.getSingle(e);
            }
        } else if (skriptItem instanceof Slot) {
            Expression<?> expr = expression.getConvertedExpression(Slot.class);
            if (expr != null) {
                Slot s = (Slot) expr.getSingle(e);
                if (s != null) {
                    return s.getItem();
                }
            }
        } else if (skriptItem instanceof ItemType) {
            Expression<?> expr = expression.getConvertedExpression(ItemType.class);
            if (expr != null) {
                ItemType s = (ItemType) expr.getSingle(e);
                if (s != null) {
                    return s.getRandom();
                }
            }
        }
        return null;
    }

    static JsonElement parseObject(Object skriptItem, Expression<?> expression, Event e) {
        if (skriptItem instanceof JsonElement) {
            return  (JsonElement) skriptItem;
        } else if (isClassicType(skriptItem)) {
            return fromString2JsonElement(skriptItem.toString());
        } else {
            if (skriptItem instanceof ItemType || skriptItem instanceof Slot || skriptItem instanceof ItemStack) {
                return JsonAdapter.toJson(parseItem(skriptItem, expression, e));
            } else {
                return JsonAdapter.toJson(skriptItem);
            }
        }
    }

    String GSON_GENERIC_ADAPTER_KEY = "??";

    /**
     * <p>
     * This method will return a deserialization Object {@link T}
     *
     * @return JsonElement
     * </p>
     */
    @NotNull JsonElement toJson(T object);

    /**
     * <p>
     * This method will return a serialization {@link JsonElement} from {@link T}
     *
     * @return T
     * </p>
     */
    T fromJson(JsonElement json);


    /**
     * <p>
     * This method will check what type of serialized Json contain. {@link JsonElement}
     *
     * @return Clazz
     * </p>
     */
    Class<? extends T> typeOf(JsonElement json);

}