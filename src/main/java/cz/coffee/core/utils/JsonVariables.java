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
package cz.coffee.core.utils;

import ch.njol.skript.lang.Variable;
import ch.njol.skript.variables.Variables;
import com.google.gson.JsonPrimitive;
import org.bukkit.event.Event;

public class JsonVariables {

    /**
     * This function convert from JsonPrimitive to Object and then set it to Variable.
     *
     * @param name    Variable name
     * @param element Any {@link JsonPrimitive}
     * @param event   {@link Event}
     * @param isLocal {@link Boolean} if a variable is local or nah
     */
    public static void setPrimitiveType(String name, JsonPrimitive element, Event event, boolean isLocal) {
        if (element.isBoolean()) {
            setVariable(name, element.getAsBoolean(), event, isLocal);
        } else if (element.isNumber()) {
            setVariable(name, element.getAsDouble(), event, isLocal);
        } else if (element.isString()) {
            setVariable(name, element.getAsString(), event, isLocal);
        }
    }

    /**
     * This function setting the value to variable
     *
     * @param name    Variable name
     * @param element Any {@link JsonPrimitive}
     * @param event   {@link Event}
     * @param isLocal {@link Boolean} if a variable is local or nah
     */
    public static void setVariable(String name, Object element, Event event, boolean isLocal) {
        Variables.setVariable(name, element, event, isLocal);
    }

    /**
     * This function will get data from variable.
     *
     * @param name    Variable name
     * @param isLocal {@link Boolean} if a variable is local or nah
     * @return {@link Object}
     */
    public static Object getVariable(Event e, String name, boolean isLocal) {
        final Object variable = Variables.getVariable(name, e, isLocal);
        if (variable == null) {
            return Variables.getVariable((isLocal ? Variable.LOCAL_VARIABLE_TOKEN : "") + name, e, false);
        }
        return variable;
    }
}
