package cz.coffee.skript.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import cz.coffee.core.utils.JsonFile;
import org.jetbrains.annotations.NotNull;

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
 * Created: pátek (17.03.2023)
 */

@Since("2.8.0 - performance & clean")
public class JsonFileType {
    static {
        Classes.registerClass(
                new ClassInfo<>(JsonFile.class, "jsonfile")
                        .user("jsonfile")
                        .name("json file")
                        .description("Represent the json file class")
                        .since("2.8.0 - performance & clean")
                        .defaultExpression(new SimpleLiteral<>(new JsonFile(), true))
                        .parser(new Parser<>() {
                            @Override
                            public @NotNull String toString(JsonFile o, int flags) {
                                return o.getName();
                            }

                            @Override
                            public @NotNull String toVariableNameString(JsonFile o) {
                                return toString(o, 0);
                            }

                            @Override
                            public boolean canParse(@NotNull ParseContext context) {
                                return false;
                            }
                        })
        );
    }
}
