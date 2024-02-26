package cz.coffee.skjson.skript.types;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.*;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Version;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.yggdrasil.Fields;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import cz.coffee.skjson.api.requests.Request;
import cz.coffee.skjson.api.requests.RequestMethod;
import cz.coffee.skjson.api.requests.Webhook;
import cz.coffee.skjson.json.JsonParser;
import cz.coffee.skjson.parser.ParserUtil;
import cz.coffee.skjson.utils.PatternUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converters;

import java.io.StreamCorruptedException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static cz.coffee.skjson.api.ConfigRecords.PATH_VARIABLE_DELIMITER;
import static cz.coffee.skjson.api.ConfigRecords.PROJECT_DEBUG;
import static cz.coffee.skjson.parser.ParserUtil.defaultConverter;
import static cz.coffee.skjson.parser.ParserUtil.parse;
import static cz.coffee.skjson.utils.Logger.error;
import static cz.coffee.skjson.utils.PatternUtil.keyStruct;

@Since("2.9")
@SuppressWarnings("deprecation")
abstract class Types {

    // JsonElement type
    static final Collection<Class<?>> allowedTypes = List.of(
            ItemStack.class, Location.class, World.class, Chunk.class, Inventory.class, ConfigurationSerializable.class
    );

    static {
        try {
            if (Skript.getVersion().isLargerThan(new Version(2, 6, 4))) {
                allowedTypes.forEach(clazz -> Converters.registerConverter(JsonElement.class, clazz, ParserUtil::from));
            } else {
                allowedTypes.forEach(clazz -> ch.njol.skript.registrations.Converters.registerConverter(JsonElement.class, clazz, ParserUtil::from));

            }
        } catch (Exception e) {
            if (PROJECT_DEBUG) error(e);
        }

         /*
         Registrations for JsonElement
          */

        Classes.registerClass(
                new ClassInfo<>(JsonElement.class, "json")
                        .user("json")
                        .name("json")
                        .description("JSON representation in skript")
                        .since("2.9")
                        .parser(new Parser<>() {
                            @Override
                            public @NotNull String toString(JsonElement o, int flags) {
                                return o.toString();
                            }

                            @Override
                            public @NotNull String toVariableNameString(JsonElement o) {
                                return toString(o, 1);
                            }

                            @Override
                            public boolean canParse(@NotNull ParseContext context) {
                                return false;
                            }
                        })
                        .serializer(new Serializer<>() {
                            @Override
                            public @NotNull Fields serialize(JsonElement o) {
                                Fields fields = new Fields();
                                fields.putObject("json", o.toString());
                                return fields;
                            }

                            @Override
                            public void deserialize(JsonElement o, @NotNull Fields f) {
                                assert false;
                            }

                            @Override
                            public JsonElement deserialize(@NotNull Fields fields) throws StreamCorruptedException {
                                Object field = fields.getObject("json");
                                if (field == null) return JsonNull.INSTANCE;
                                fields.removeField("json");
                                return defaultConverter(field);
                            }

                            @Override
                            public boolean mustSyncDeserialization() {
                                return false;
                            }

                            @Override
                            protected boolean canBeInstantiated() {
                                return false;
                            }
                        })
                        .changer(new Changer<>() {
                            @Override
                            @SuppressWarnings("all")
                            public @Nullable Class<?> @NotNull [] acceptChange(@NotNull ChangeMode mode) {
                                return switch (mode) {
                                    case RESET, REMOVE, REMOVE_ALL -> CollectionUtils.array(Object[].class);
                                    default -> null;
                                };
                            }

                            @Override
                            // mainJson
                            public void change(JsonElement @NotNull [] jsonInput, @Nullable Object @NotNull [] dataInput, @NotNull ChangeMode mode) {
                                switch (mode) {
                                    case REMOVE -> {
                                        for (JsonElement mainJson : jsonInput) {
                                            for (Object unparsed : dataInput) {
                                                try {
                                                    if (unparsed instanceof JsonElement jsonData) {
                                                        if (JsonParser.isExpression(jsonData)) {
                                                            if (jsonData.isJsonObject()) {
                                                                JsonObject parsed = jsonData.getAsJsonObject();
                                                                LinkedList<keyStruct> path = new LinkedList<>();
                                                                String pathString = parsed.get("element-path").getAsString();
                                                                String index = parsed.get("element-index").toString();
                                                                if (!pathString.equals("Undefined")) {
                                                                    path = PatternUtil.convertStringToKeys(pathString, PATH_VARIABLE_DELIMITER);
                                                                    assert !path.isEmpty();
                                                                }
                                                                path.add(new keyStruct(index, PatternUtil.KeyType.KEY));
                                                                JsonParser.remove(mainJson).byIndex(path);
                                                            }
                                                        }
                                                    } else {
                                                        if (unparsed instanceof List<?> list) {
                                                            String type = (String) list.get(0);
                                                            String pathString = (String) list.get(2);
                                                            LinkedList<keyStruct> path = new LinkedList<>();
                                                            Object[] items = (Object[]) list.get(1);
                                                            if (type.equalsIgnoreCase("object")) {
                                                                boolean isValue = (boolean) list.get(3);
                                                                if (!pathString.equals("Undefined")) {
                                                                    path = PatternUtil.convertStringToKeys(pathString, PATH_VARIABLE_DELIMITER, false);
                                                                    assert !path.isEmpty();
                                                                    for (Object item : items) {
                                                                        JsonElement parsed = parse(item);
                                                                        if (isValue) {
                                                                            JsonParser.remove(mainJson).byValue(path, parsed);
                                                                        } else {
                                                                            path.add(new keyStruct(item.toString(), PatternUtil.KeyType.KEY));
                                                                            JsonParser.remove(mainJson).byKey(path);
                                                                        }
                                                                    }
                                                                } else {
                                                                    for (Object item : items) {
                                                                        JsonElement parsed = parse(item);
                                                                        if (isValue) {
                                                                            JsonParser.remove(mainJson).byValue(path, parsed);
                                                                        } else {
                                                                            path.add(new keyStruct(item.toString(), PatternUtil.KeyType.KEY));
                                                                            JsonParser.remove(mainJson).byKey(path);
                                                                        }
                                                                    }
                                                                }
                                                            } else if (type.equalsIgnoreCase("array")) {
                                                                if (!pathString.equals("Undefined")) {
                                                                    path = PatternUtil.convertStringToKeys(pathString, PATH_VARIABLE_DELIMITER);
                                                                    assert !path.isEmpty();
                                                                    for (Object item : items) {
                                                                        JsonElement parsed = parse(item);
                                                                        JsonParser.remove(mainJson).byValue(path, parsed);
                                                                    }
                                                                } else {
                                                                    for (Object item : items) {
                                                                        JsonParser.remove(mainJson).byValue(path, parse(item));
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                } catch (Exception ex) {
                                                    if (PROJECT_DEBUG)
                                                        error(ex);
                                                }
                                            }
                                        }
                                    }
                                    case REMOVE_ALL -> {
                                        for (JsonElement mainJson : jsonInput) {
                                            for (Object unparsed : dataInput) {
                                                try {
                                                    if (unparsed instanceof List<?> list) {
                                                        String pathString = (String) list.get(2);
                                                        Object[] items = (Object[]) list.get(1);
                                                        LinkedList<keyStruct> path;
                                                        if (!pathString.equals("Undefined")) {
                                                            for (Object item : items) {
                                                                JsonElement parsed = parse(item);
                                                                path = PatternUtil.convertStringToKeys(pathString, PATH_VARIABLE_DELIMITER);
                                                                //child
                                                                JsonParser.remove(mainJson).allByValue(path, parsed);
                                                            }
                                                        } else {
                                                            for (Object item : items) {
                                                                JsonElement parsed = parse(item);
                                                                //root
                                                                JsonParser.remove(mainJson).allByValue(null, parsed);
                                                            }
                                                        }
                                                    }
                                                } catch (Exception ex) {
                                                    error(ex);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        })
        );

         /*
         Registrations for webhooks
          */


        Classes.registerClass(
                new ClassInfo<>(Webhook.class, "jsonwebhook")
                        .user("json-webhook")
                        .name("json-webhook")
                        .description("webhooks")
                        .since("2.9")
                        .parser(new Parser<>() {
                            @Override
                            public @NonNull String toString(@NonNull Webhook o, int flags) {
                                return o.toString();
                            }

                            @Override
                            public @NonNull String toVariableNameString(Webhook o) {
                                return toString(o, 0);
                            }

                            @Override
                            public boolean canParse(@NonNull ParseContext context) {
                                return false;
                            }
                        })
        );

        Classes.registerClass(new EnumClassInfo<>(RequestMethod.class, "requestmethod", "request method")
                .user("request ?method?")
                .name("Request methods")
                .description("represent allowed methods for make a request, e.g. POST, GET")
                .examples("")
                .since("2.9")
        );

        Classes.registerClass(
                new ClassInfo<>(Request.class, "request")
                        .user("request")
                        .name("request")
                        .description("Representation instance of Request")
                        .since("2.9.9-pre API changes")
                        .parser(new Parser<>() {
                            @Override
                            public @NotNull String toString(Request request, int i) {
                                return request.toString();
                            }

                            @Override
                            public @NotNull String toVariableNameString(Request request) {
                                return request.toString();
                            }

                            @Override
                            public boolean canParse(@NonNull ParseContext context) {
                                return false;
                            }
                        })
        );
    }


}
