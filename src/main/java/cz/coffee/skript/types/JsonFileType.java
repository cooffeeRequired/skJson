package cz.coffee.skript.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import cz.coffee.core.utils.JsonFile;
import org.eclipse.jdt.annotation.NonNull;

@NoDoc
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
                            public @NonNull String toString(@NonNull JsonFile o, int flags) {
                                return o.getName();
                            }

                            @Override
                            public @NonNull String toVariableNameString(JsonFile o) {
                                return toString(o, 0);
                            }

                            @Override
                            public boolean canParse(@NonNull ParseContext context) {
                                return false;
                            }
                        })
        );
    }
}
