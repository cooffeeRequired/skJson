package cz.coffeerequired.modules;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.registrations.EventValues;
import com.google.gson.JsonElement;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.Extensible;
import cz.coffeerequired.api.Register;
import cz.coffeerequired.api.annotators.Module;
import cz.coffeerequired.api.json.GsonParser;
import cz.coffeerequired.api.json.Json;
import cz.coffeerequired.api.types.JsonPath;
import cz.coffeerequired.skript.core.bukkit.JsonFileChanged;
import cz.coffeerequired.skript.core.conditions.*;
import cz.coffeerequired.skript.core.effects.*;
import cz.coffeerequired.skript.core.eventexpressions.ExprEvtFile;
import cz.coffeerequired.skript.core.eventexpressions.ExprEvtJson;
import cz.coffeerequired.skript.core.eventexpressions.ExprEvtUUID;
import cz.coffeerequired.skript.core.events.WatcherEvent;
import cz.coffeerequired.skript.core.expressions.*;
import cz.coffeerequired.skript.core.support.JsonLoopExpression;
import cz.coffeerequired.skript.core.support.JsonSupportElements;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.skriptlang.skript.lang.converter.Converters;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.UUID;


@Module(module = "core")
public class Core extends Extensible {

    static final Collection<Class<?>> allowedTypes = List.of(
            ConfigurationSerializable.class,
            ItemStack.class,
            Location.class,
            World.class,
            Chunk.class,
            JsonElement.class,
            Inventory.class
    );

    public Core() {
        this.sign = this.getClass().getSimpleName();
        this.skriptElementPath = "cz.coffeerequired.skript.core";
    }

    public void tryRegisterDefaultConverters() {
        try {
            allowedTypes.forEach(type -> Converters.registerConverter(JsonElement.class, type, GsonParser::fromJson));
        } catch (Exception e) {
            SkJson.exception(e, "Error while registering default converters: %s", e.getMessage());
        }
    }

    @Override
    public void registerElements(Register.SkriptRegister register) {
        register.apply(this);

        this.tryRegisterDefaultConverters();

        // ################ TYPES ############################
        register.registerType(new ClassInfo<>(JsonElement.class, "jsonelement")
                        .user("json ?elements?")
                        .name("jsonelement")
                        .description("Json element representation")
                        .since("2.9, 4.1 - change")
                        .parser(Json.parser)
                        .serializer(Json.serializer)
                        .changer(Json.changer),
                "type.json"
        );

        register.registerType(new ClassInfo<>(JsonPath.class, "jsonpath")
                        .user("json? path?")
                        .name("json path")
                        .description("Json path representation")
                        .since("4.1 - API UPDATE")
                        .parser(JsonPath.parser)
                        .serializer(JsonPath.serializer)
                        .changer(JsonPath.changer),
                "type.jsonpath"
        );

        // ################ EXPRESSIONS ############################
        register.registerExpression(ExprNewJson.class, JsonElement.class, ExpressionType.SIMPLE,
                "json from file %strings%",
                "json from website %strings%",
                "json (from|of) %objects%"
        );
        register.registerExpression(ExprPrettyPrint.class, String.class, ExpressionType.SIMPLE,
                "%jsonelement% as pretty[ printed]",
                "%jsonelement% as uncolo[u]red pretty[ printed]"
        );
        register.registerExpression(ExprJsonPath.class, JsonPath.class, ExpressionType.SIMPLE,
                "json path %string% in %jsonelement%"
        );
        register.registerExpression(ExprChanger.class, Object.class, ExpressionType.SIMPLE,
                "(1:value|0:key) of %jsonpath%"
        );
        register.registerExpression(ExprStrictLiteralJson.class, Object.class, ExpressionType.PATTERN_MATCHES_EVERYTHING,
                "[literal] %jsonelement%.<([\\p{L}\\d_%\\[\\]*]+|\"[^\"]*\")(\\\\[\\\\]|\\\\[\\\\d+\\\\])?(\\\\.)?>",
                "[literal] %jsonelement%<\\[\\d+\\]>"
        );

        register.registerExpression(ExprRemoveValKey.class, JsonElement.class, ExpressionType.SIMPLE, "(:value[s]|:key[s]) %objects%");

        register.registerExpression(JsonSupportElements.class, Object.class, ExpressionType.COMBINED,
                "(1st|first) (:value|:key) of %jsonelement%",
                "(2nd|second) (:value|:key) of %jsonelement%",
                "(3rd|third) (:value|:key) of %jsonelement%",
                "last (:value|:key) of %jsonelement%",
                "random (:value|:key) of %jsonelement%",
                "%integer%. (:value|:key) of %jsonelement%"
        );
        register.registerExpression(ExprGetAllKeys.class, String.class, ExpressionType.SIMPLE, "[all] keys [%-string%] of %jsonelement%");

        register.registerExpression(JsonLoopExpression.class, Object.class, ExpressionType.SIMPLE, "[the] json-(:key|:value)[-<(\\d+)>]");

        register.registerExpression(ExprCountElements.class, Integer.class, ExpressionType.SIMPLE, "[the] count of (:key[s]|:value[s]) %object% in %jsonelement%");
        register.registerExpression(ExprJsonValues.class, Object.class, ExpressionType.COMBINED,
                "value [%-string%] of %jsonelement%",
                "values [%-string%] of %jsonelement%"
        );
        register.registerPropertyExpression(ExprFormattingJsonToVariable.class, JsonElement.class, "form[atted [json]]", "jsonelements");
        register.registerPropertyExpression(ExprJsonSize.class, Integer.class, "json size", "jsonelements");
        register.registerExpression(ExprAllJsonFiles.class, String.class, ExpressionType.COMBINED, "[all] json [files] (from|in) (dir[ectory]|folder) %string%");
        register.registerExpression(ExprGetCacheStorage.class, JsonElement.class, ExpressionType.SIMPLE, "json storage of id %string%", "all json storages");

        // ################ CONDITIONS ############################
        register.registerCondition(CondJsonHas.class,
                "[json] %jsonelement% has [:all] (:value[s]|:key[s]) %objects%",
                "[json] %jsonelement% does(n't| not) have [:all] (:value[s]|:key[s]) %objects%"
        );
        register.registerCondition(CondJsonType.class,
                "json type of %jsonelement% is (json[ ](:object)|json[ ](:array)|json[ ](:primitive)|json[ ](:null))",
                "json type of %jsonelement% (is(n't| not)) (json[ ](:object)|json[ ](:array)|json[ ](:primitive)|json[ ](:null))"
        );

        register.registerCondition(CondJsonFileExist.class, "json file %string% exists", "json file %string% does(n't| not) exist");
        register.registerCondition(CondJsonIsEmpty.class, "json %jsonelement% is empty", "json %jsonelement% is(n't| not) empty");
        register.registerCondition(CondIsCached.class, "[the] json storage [with [the] id] %string% is cached", "[the] json storage [with [the] id] %string% is(n't| not) cached");
        register.registerCondition(CondIsListened.class, "[the] json storage [with [the] id] %string% is listened", "[the] json storage [with [the] id] %string% is(n't| not) listened");

        // ################ EFFECTS ############################
        register.registerEffect(EffNewFile.class, "new json file %~string%", "new json file %~string% with [content] %-objects%");
        register.registerEffect(EffMapJson.class, "[:async] (map|copy) %jsonelement% to %objects%");
        register.registerEffect(EffNewFile.class,
                "create json file %string% [:with configuration<\\[\\s*((\\w+)=([\\w-]+)(?:,\\s*)?)+\\s*\\]>]",
                "create json file %string% and write to it %object% [:with configuration<\\[\\s*((\\w+)=([\\w-]+)(?:,\\s*)?)+\\s*\\]>]"
        );

        // ################ CACHE ############################
        register.registerEffect(AEffHandleWatcher.class, "bind storage watcher to %string%", "unbind storage watcher from %string%");
        register.registerEffect(EffVirtualStorage.class, "create json virtual storage named %string%");
        register.registerEffect(AEffBindFile.class, "(bind|link) json file %string% as %string%", "(bind|link) json file %string% as %string% and let bind storage watcher");
        register.registerEffect(AEffUnbindFile.class, "un(bind|link) json storage id %string%");
        register.registerEffect(AEffSaveStorage.class, "save json storage [id] %string%", "save all json storages");

        // ################ EVENTS ############################
        register.registerEvent(
                "*Json watcher save", WatcherEvent.class, JsonFileChanged.class,
                "will only run when the json watcher notices a change in the file",
                "on json watcher file change",
                "2.9",
                "[json-] watcher file change", "[json-] watch save"
        );

        EventValues.registerEventValue(JsonFileChanged.class, JsonElement.class, JsonFileChanged::getJson, EventValues.TIME_NOW);
        EventValues.registerEventValue(JsonFileChanged.class, UUID.class, JsonFileChanged::getUuid, EventValues.TIME_NOW);
        EventValues.registerEventValue(JsonFileChanged.class, File.class, JsonFileChanged::getLinkedFile, EventValues.TIME_NOW);

        register.registerEventValueExpression(ExprEvtUUID.class, UUID.class, "event-(uuid|id)");
        register.registerEventValueExpression(ExprEvtFile.class, File.class, "event-(file|link)");
        register.registerEventValueExpression(ExprEvtJson.class, JsonElement.class, "event-(json|content)");
    }
}
