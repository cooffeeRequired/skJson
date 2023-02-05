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
package cz.coffee.skript.test;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.shanebeestudios.skbee.api.NBT.NBTContainer;
import cz.coffee.utils.ErrorHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static cz.coffee.adapter.DefaultAdapters.assignFrom;
import static cz.coffee.adapter.DefaultAdapters.assignTo;
import static cz.coffee.utils.ErrorHandler.sendMessage;
import static cz.coffee.utils.SimpleUtil.printPrettyStackTrace;
import static cz.coffee.utils.config.Config._STACKTRACE_LENGTH;


public class SkJsonEffectTest extends SimpleExpression<Boolean> {

    static {
        Skript.registerExpression(SkJsonEffectTest.class, Boolean.class, ExpressionType.SIMPLE,
                "[skJson] version check");
    }


    private JsonElement np(String str) {
        return JsonParser.parseString(str);
    }

    protected boolean testElements() {
        JsonElement e;
        sendMessage("Debug mode", ErrorHandler.Level.WARNING);
        sendMessage("Server Information", ErrorHandler.Level.INFO);
        sendMessage("", ErrorHandler.Level.INFO);
        sendMessage("Version: " + Bukkit.getVersion(), ErrorHandler.Level.INFO);
        sendMessage("Bukkit Version: " + Bukkit.getBukkitVersion(), ErrorHandler.Level.INFO);
        sendMessage("Minecraft Version: " + Bukkit.getMinecraftVersion(), ErrorHandler.Level.INFO);

        try {
            e = np("{'A':'n'}");
            sendMessage("&eskJson &fTest -> String parsing - &aPassed", ErrorHandler.Level.INFO);
            JsonElement e1 = np("{'A': 'N'}");
            if (e==e1) return false;
            sendMessage("&eskJson &fTest -> String case Equals - &aPassed", ErrorHandler.Level.INFO);
            ItemStack i = new ItemStack(Material.MAGENTA_DYE);
            e = assignTo(i);
            if (!(Objects.equals(assignFrom(e), i))) return false;
            sendMessage("&eskJson &fTest -> Converter parsing From/To - &aPassed", ErrorHandler.Level.INFO);

        } catch (Exception exception) {
            printPrettyStackTrace(exception, _STACKTRACE_LENGTH);
            return false;
        }
        return true;
    }


    @Override
    protected Boolean @NotNull [] get(@NotNull Event e) {
        return new Boolean[]{testElements()};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends Boolean> getReturnType() {
        return Boolean.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "skJson debug mode - Test";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        return true;
    }
}
