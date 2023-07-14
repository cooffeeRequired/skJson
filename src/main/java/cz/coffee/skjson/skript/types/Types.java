package cz.coffee.skjson.skript.types;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.*;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Version;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.yggdrasil.Fields;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import cz.coffee.skjson.api.Config;
import cz.coffee.skjson.api.discord.Webhook;
import cz.coffee.skjson.json.ParsedJson;
import cz.coffee.skjson.parser.ParserUtil;
import cz.coffee.skjson.skript.requests.Requests;
import cz.coffee.skjson.utils.Util;
import net.kyori.adventure.util.Index;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.converter.Converters;

import java.io.StreamCorruptedException;
import java.util.*;

import static cz.coffee.skjson.api.Config.LOGGING_LEVEL;
import static cz.coffee.skjson.api.Config.PROJECT_DEBUG;
import static cz.coffee.skjson.parser.ParserUtil.checkKeys;
import static cz.coffee.skjson.parser.ParserUtil.defaultConverter;

@Since("2.9")
abstract class Types {
    // JsonElement type
     static final Collection<Class<?>> allowedTypes = List.of(ItemStack.class, Location.class, World.class, Chunk.class, Inventory.class, ConfigurationSerializable.class);

     static {
         try {
             if (Skript.getVersion().isLargerThan(new Version(2, 6, 4))) {
                 allowedTypes.forEach(clazz -> Converters.registerConverter(JsonElement.class, clazz, ParserUtil::from));
             } else {
                 allowedTypes.forEach(clazz -> ch.njol.skript.registrations.Converters.registerConverter(JsonElement.class, clazz, ParserUtil::from));

             }
         } catch (Exception e) {if (PROJECT_DEBUG) Util.error(e.getMessage(), ErrorQuality.NONE);}

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
                                 return toString(o, 0);
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
                             public void change(JsonElement @NotNull [] jsonInput, @Nullable Object @NotNull [] dataInput, @NotNull ChangeMode mode) {
                                 switch (mode) {
                                     case REMOVE -> {
                                         ParsedJson pj = null;
                                         for (JsonElement mainJson : jsonInput) {
                                             for (Object unparsed : dataInput) {
                                                 try {
                                                     pj = new ParsedJson(mainJson);
                                                     if (unparsed instanceof JsonElement jsonData) {
                                                         if (new ParsedJson(jsonData).isExpression()) {
                                                             if (jsonData.isJsonObject()) {
                                                                 JsonObject parsed = jsonData.getAsJsonObject();
                                                                 LinkedList<String> path = new LinkedList<>();
                                                                 String pathString = parsed.get("element-path").getAsString();
                                                                 String index = parsed.get("element-index").toString();
                                                                 if (!pathString.equals("Undefined")) {
                                                                     path = Util.extractKeysToList(pathString, Config.PATH_VARIABLE_DELIMITER);
                                                                     assert path != null;
                                                                 }
                                                                 path.add(index);
                                                                 pj.removeByIndex(path);
                                                             }
                                                         }
                                                     } else {
                                                         if (unparsed instanceof List<?> list) {
                                                             String type = (String) list.get(0);
                                                             String pathString = (String) list.get(2);
                                                             LinkedList<String> path = new LinkedList<>();
                                                             Object[] items = (Object[]) list.get(1);
                                                             if (type.equalsIgnoreCase("object")) {
                                                                 boolean isValue = (boolean) list.get(3);
                                                                 if (!pathString.equals("Undefined")) {
                                                                     path = Util.extractKeysToList(pathString, Config.PATH_VARIABLE_DELIMITER);
                                                                     assert path != null;
                                                                     for (Object item : items) {
                                                                         JsonElement parsed = ParserUtil.parse(item);
                                                                         if (isValue) {;
                                                                             pj.removeByValue(path, parsed);
                                                                         } else {
                                                                             path.add(item.toString());
                                                                             pj.removeByKey(path);
                                                                         }
                                                                     }
                                                                 } else {
                                                                     for (Object item : items) {
                                                                         JsonElement parsed = ParserUtil.parse(item);
                                                                         if (isValue) {
                                                                             pj.removeByValue(path, parsed);
                                                                         } else {
                                                                             path.add(item.toString());
                                                                             pj.removeByKey(path);
                                                                         }
                                                                     }
                                                                 }
                                                             } else if (type.equalsIgnoreCase("array")) {
                                                                 if (!pathString.equals("Undefined")) {
                                                                     path = Util.extractKeysToList(pathString, Config.PATH_VARIABLE_DELIMITER);
                                                                     assert path != null;
                                                                     for (Object item : items) {
                                                                         JsonElement parsed = ParserUtil.parse(item);
                                                                         pj.removeByValue(path, parsed);
                                                                     }
                                                                 } else {
                                                                     for (Object item : items) {
                                                                         JsonElement parsed = ParserUtil.parse(item);
                                                                         pj.removeByValue(path, parsed);
                                                                     }
                                                                 }
                                                             }
                                                         }
                                                     }
                                                 } catch (Exception ex) {
                                                     if (LOGGING_LEVEL > 1 && !PROJECT_DEBUG) {
                                                         Util.error("Something happened in the Changer! If you wanna more information");
                                                         if (!PROJECT_DEBUG) Util.error("Turn on debug in your config.");
                                                         else
                                                             Util.enchantedError(ex, ex.getStackTrace(), "Types-Changer[0]");
                                                     }
                                                     if (PROJECT_DEBUG) Util.enchantedError(ex, ex.getStackTrace(), "Types-Changer[0]");
                                                 }
                                             }
                                         }
                                     }
                                     case REMOVE_ALL -> {
                                         ParsedJson pj = null;
                                         for (JsonElement mainJson : jsonInput) {
                                             for (Object unparsed : dataInput) {
                                                 try {
                                                     pj = new ParsedJson(mainJson);
                                                     if (unparsed instanceof List<?> list) {
                                                         String type = (String) list.get(0);
                                                         String pathString = (String) list.get(2);
                                                         Object[] items = (Object[]) list.get(1);
                                                         LinkedList<String> path;
                                                         if (!pathString.equals("Undefined")) {
                                                             for (Object item : items) {
                                                                 JsonElement parsed = ParserUtil.parse(item);
                                                                 path = Util.extractKeysToList(pathString, Config.PATH_VARIABLE_DELIMITER);
                                                                 //child
                                                                 pj.removeAllByValue(path, parsed);
                                                             }
                                                         } else {
                                                             for (Object item : items) {
                                                                 JsonElement parsed = ParserUtil.parse(item);
                                                                 //root
                                                                 pj.removeAllByValue(null, parsed);
                                                             }
                                                         }
                                                     }
                                                 } catch (Exception ex) {
                                                     ex.printStackTrace();
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

         Classes.registerClass(new EnumClassInfo<>(Requests.RequestMethods.class, "requestmethod", "request method")
                 .user("request ?method?")
                 .name("Request methods")
                 .description("represent allowed methods for make a request, e.g. POST, GET")
                 .examples("")
                 .since("2.9")
         );
     }


}
