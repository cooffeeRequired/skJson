package cz.coffee.core.adapters;

import com.google.gson.JsonElement;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;

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
 * Created: Saturday (3/4/2023)
 */

public class InventoryData {
    private InventoryHolder holder;
    private InventoryType type;
    private int size;
    private String title;

    private Map<String, JsonElement> contents;

    protected InventoryHolder getHolder() {
        return holder;
    }

    protected void setHolder(InventoryHolder holder) {
        this.holder = holder;
    }

    protected InventoryType getType() {
        return type;
    }

    protected void setType(InventoryType type) {
        this.type = type;
    }

    protected int getSize() {
        return size;
    }

    protected void setSize(int size) {
        this.size = size;
    }

    protected String getTitle() {
        return title;
    }

    protected void setTitle(String title) {
        this.title = title;
    }

    protected Map<String, JsonElement> getContents() {
        return contents;
    }

    protected void setContents(Map<String, JsonElement> contents) {
        this.contents = contents;
    }
}