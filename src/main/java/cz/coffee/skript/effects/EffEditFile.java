package cz.coffee.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.lang.function.SimpleJavaFunction;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.registrations.DefaultClasses;
import ch.njol.skript.util.AsyncEffect;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import cz.coffee.core.utils.FileUtils;
import cz.coffee.core.utils.JsonFile;
import cz.coffee.core.utils.JsonUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.LinkedList;
import java.util.Objects;

import static cz.coffee.core.utils.AdapterUtils.parseItem;
import static cz.coffee.core.utils.Util.extractKeys;

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
 * Created: úterý (14.03.2023)
 */

@Name("change json file")
@Description({"You can change json file."})
@Examples({
        "command jsonFileValue:",
        "\ttrigger:",
        "\t\tchange key \"A\" of jsonfile(\"plugins/<>/test.json\") to \"B\""
})
@Since("2.8.0 - performance & clean")


public class EffEditFile extends AsyncEffect {

    static {
        Skript.registerEffect(EffEditFile.class,
                "[:async] change (:value|:key) %string% of (%-jsonfile/string%|(:json file) %-string%) to %objects%"
        );
        Parameter<?>[] fileInput = new Parameter[]{new Parameter<>("file", DefaultClasses.STRING, true, null)};
        Functions.registerFunction(new SimpleJavaFunction<>("jsonfile", fileInput, DefaultClasses.OBJECT, true) {
            @Override
            public @Nullable JsonFile @NotNull [] executeSimple(@NotNull Object[][] params) {
                return new JsonFile[]{new JsonFile(String.valueOf(params[0][0]))};
            }
        }.description("Get json file from string/object input")
                .examples("jsonfile(\"plugins/test/test.json\")")
                .since("2.8.0 - performance & clean"));
    }
    private Expression<?> fileExpression, expressionObjects;
    private Expression<String> pathExpression;
    private boolean isValue, jsonFileExpression, isArray, async;

    @Override
    protected void execute(@NotNull Event e) {
        File file;
        if (jsonFileExpression) {
            file = new File((String) Objects.requireNonNull(fileExpression.getSingle(e)));
        } else {
            file = (JsonFile) fileExpression.getSingle(e);
        }

        if (file == null) return;
        final JsonElement oldJson = FileUtils.get(file);
        final String pathString = pathExpression.getSingle(e);
        if (pathString != null) {
            JsonElement parsedElement = null;
            LinkedList<String> keys = extractKeys(pathString, null);
            if (keys == null) return;
            Object[] objects = expressionObjects.getAll(e);
            if (isValue) {
                JsonArray array = new JsonArray();
                for (Object object : objects) {
                    if (isArray) {
                        array.add(parseItem(object, expressionObjects, e));
                        parsedElement = array;
                    } else {
                        parsedElement = parseItem(object, expressionObjects, e);
                    }
                }
                JsonUtils.changeValue(oldJson, keys, parsedElement);
            } else {
                String st = (String) objects[0];
                JsonUtils.changeKey(oldJson, keys, st);
            }
        }
        FileUtils.write(file, oldJson, async);
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return String.format("change %s of %s to %s", (isValue ? "value" : "key"), fileExpression.toString(e, debug), expressionObjects.toString(e, debug));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        getParser().setHasDelayBefore(Kleenean.TRUE);
        isValue = parseResult.hasTag("value");
        async = parseResult.hasTag("async");
        jsonFileExpression = parseResult.hasTag("json file");
        pathExpression = (Expression<String>) exprs[0];

        if (jsonFileExpression) {
            fileExpression = LiteralUtils.defendExpression(exprs[2]);
            if (!LiteralUtils.canInitSafely(fileExpression) || !fileExpression.getReturnType().equals(String.class)) {
                Skript.error("Hey! You use the `key` delimiter, but the input is not a string.", ErrorQuality.SEMANTIC_ERROR);
                return false;
            }
        } else {
            fileExpression = LiteralUtils.defendExpression(exprs[1]);
            if (!LiteralUtils.canInitSafely(fileExpression) || !fileExpression.getReturnType().isAssignableFrom(JsonFile.class)) {
                Skript.error("Hey! If you want use raw expression without a `json file %string%` then you can use that only for json-file, you can get that by e.g. `jsonfile(\"plugins/<>/test.json\")`", ErrorQuality.SEMANTIC_ERROR);
                return false;
            }
        }

        expressionObjects = LiteralUtils.defendExpression(exprs[3]);
        if (!isValue) {
            if (expressionObjects.getReturnType().equals(String.class)) {
                isArray = expressionObjects.isSingle();
            } else {
                Skript.error("Hey! you are trying to use an object other than String to change the key. You can only change the key using String.", ErrorQuality.SEMANTIC_ERROR);
                return false;
            }
        }
        return LiteralUtils.canInitSafely(expressionObjects);
    }
}
