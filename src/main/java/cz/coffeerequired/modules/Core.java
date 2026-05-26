package cz.coffeerequired.modules;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.util.Version;
import com.google.gson.JsonElement;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.Extensible;
import cz.coffeerequired.api.Register;
import cz.coffeerequired.api.annotators.Module;
import cz.coffeerequired.api.json.Json;
import cz.coffeerequired.api.json.Parser;
import cz.coffeerequired.skript.core.bukkit.JsonFileChanged;
import cz.coffeerequired.skript.core.conditions.*;
import cz.coffeerequired.skript.core.effects.*;
import cz.coffeerequired.skript.core.expressions.ExprJson;
import cz.coffeerequired.skript.core.support.ExprJsonLoop;
import cz.coffeerequired.skript.core.events.WatcherEvent;
import cz.coffeerequired.skript.core.expressions.*;
import cz.coffeerequired.skript.core.support.JsonLoopExpression;
import cz.coffeerequired.skript.core.support.JsonSupportElements;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.skriptlang.skript.lang.converter.Converters;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.UUID;


@SuppressWarnings({"removal", "deprecation"})
@Module(module = "core")
public class Core extends Extensible {

    static final Collection<Class<?>> allowedTypes = List.of(
            ConfigurationSerializable.class,
            ItemStack.class,
            Location.class,
            World.class,
            Chunk.class,
            JsonElement.class,
            Inventory.class,
            Player.class
    );

    public Core() {
        this.sign = this.getClass().getSimpleName();
        this.skriptElementPath = "cz.coffeerequired.skript.core";
    }

    public void tryRegisterDefaultConverters() {
        try {
            if (Skript.getVersion().isLargerThan(new Version(2, 10))) {
                allowedTypes.forEach(type -> Converters.registerConverter(JsonElement.class, type, element -> convertJsonElement(element, type)));
            } else {
                allowedTypes.forEach(type -> ch.njol.skript.registrations.Converters.registerConverter(JsonElement.class, type, element -> convertJsonElement(element, type)));
            }
        } catch (Exception e) {
            SkJson.exception(e, "Error while registering default converters: %s", e.getMessage());
        }
    }

    private static <T> T convertJsonElement(JsonElement element, Class<T> targetType) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        Object parsed = Parser.fromJson(element);
        if (parsed == null) {
            return null;
        }
        if (targetType.isInstance(parsed)) {
            return targetType.cast(parsed);
        }
        return null;
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

        // ################ EXPRESSIONS ############################
        register.registerExpression(ExprNewJson.class, JsonElement.class, ExpressionType.SIMPLE,
                "json from file %strings%",
                "json (loaded|read) from [the] file[s] %strings%",
                "load json from file %strings%",
                "json from [the] (website|url) %strings%",
                "json from url %strings%",
                "fetch json from [url] %strings%",
                "json (from|of) %objects%",
                "parse %objects% as json",
                "json (parsed|created) from %objects%"
        );
        register.registerExpression(ExprJsonCopy.class, JsonElement.class, ExpressionType.SIMPLE,
                "[a] deep copy of %jsonelement%",
                "[a] copy of %jsonelement%",
                "%jsonelement% (deeply )copied"
        );
        register.registerExpression(ExprPrettyPrint.class, String.class, ExpressionType.SIMPLE,
                "%jsonelement% as pretty[ printed]",
                "%jsonelement% as (pretty[ printed]|formatted) json",
                "%jsonelement% as uncolo[u]red pretty[ printed]",
                "%jsonelement% as unformatted pretty[ printed]",
                "%jsonelement% as (compact|minified) json",
                "%jsonelement% as compact json"
        );

        if (Register.isClassAvailable("com.btk5h.skriptmirror.SkriptMirror")) {
            SkJson.warning("You are using Skript-reflect, which is not compatible with this expression. Please do not use&c 'literal <json element>[<index>]'&6&l but use instead of it &f'<json element>.<index>'&6&l for arrays.\n And for objects use please&f 'literal <json element>.<key>'.");
        }
        register.registerExpression(ExprStrictLiteralJson.class, Object.class, ExpressionType.PATTERN_MATCHES_EVERYTHING,
                "[literal] %jsonelement%.<([\\p{L}\\d_%\\[\\]*]+|\"[^\"]*\")(\\\\[\\\\]|\\\\[\\\\d+\\\\])?(\\\\.)?>",
                "[literal] %jsonelement%<\\[\\d+\\]>"
        );

        register.registerExpression(JsonSupportElements.class, Object.class, ExpressionType.COMBINED,
                "(1st|first) (:value|:key) of %jsonelement%",
                "(2nd|second) (:value|:key) of %jsonelement%",
                "(3rd|third) (:value|:key) of %jsonelement%",
                "last (:value|:key) of %jsonelement%",
                "random (:value|:key) of %jsonelement%",
                "%integer%. (:value|:key) of %jsonelement%"
        );
        register.registerExpression(ExprGetAllKeys.class, String.class, ExpressionType.SIMPLE,
                "[all] keys [%-string%] of %jsonelement%",
                "[every|all] key[s] (at|in) [path] [%-string%] (of|in) %jsonelement%",
                "keys (at|in) [path] [%-string%] (of|in) %jsonelement%"
        );

        register.registerExpression(JsonLoopExpression.class, Object.class, ExpressionType.SIMPLE,
                "[the] json-(:key|:value)[-<(\\d+)>]",
                "[the] json loop-(:key|:value)[-<(\\d+)>]"
        );

        register.registerExpression(ExprCountElements.class, Integer.class, ExpressionType.SIMPLE,
                "[the] count of (:key[s]|:value[s]) %object% in %jsonelement%",
                "[the] number of (:key[s]|:value[s]) %object% in %jsonelement%"
        );
        register.registerExpression(ExprJsonValues.class, Object.class, ExpressionType.COMBINED,
                "[json] :value [%-string%] of %jsonelement%",
                "[json] :values [%-string%] of %jsonelement%",
                "value at path [%-string%] in %jsonelement%",
                ":values at path [%-string%] in %jsonelement%",
                "value of path [%-string%] in %jsonelement%",
                ":values of path [%-string%] in %jsonelement%",
                "path %string% of %jsonelement%",
                "the path %string% in %jsonelement%",
                "%jsonelement%'s path %string%",
                "%jsonelement%'s value at path %string%"
        );
        register.registerExpression(ExprArrayObject.class, Object.class, ExpressionType.SIMPLE,
                "json (:array|:object) %string% of %jsonelement%",
                "json (:array|object) at path %string% (of|in) %jsonelement%"
        );
        register.registerPropertyExpression(ExprFormattingJsonToVariable.class, JsonElement.class, "form[atted [json]]", "jsonelements");
        register.registerPropertyExpression(ExprJsonSize.class, Integer.class, "json size", "jsonelements");
        register.registerExpression(ExprAllJsonFiles.class, File.class, ExpressionType.COMBINED,
                "[all] json [files] (from|in) (dir[ectory]|folder) %string%",
                "json files in [the] (folder|directory) %string%",
                "all json files in %string%"
        );
        register.registerExpression(ExprGetCacheStorage.class, JsonElement.class, ExpressionType.SIMPLE,
                "json storage of id %-string%",
                "cached json [with id] %-string%",
                "json cache %-string%",
                "all json storages",
                "all cached json",
                "all json caches"
        );
        register.registerExpression(ExprSortJson.class, JsonElement.class, ExpressionType.SIMPLE,
                "%jsonelement% [sorted] in (:ascending|:descending) order by (:key|:value)",
                "%jsonelement% sorted by (:key|value) in (:ascending|descending) order"
        );

        // ################ CONDITIONS ############################
        register.registerCondition(CondJsonHas.class,
                "[json] %jsonelement% has [:all] (:value[s]|:key[s]) %objects%",
                "[json] %jsonelement% does(n't| not) have [:all] (:value[s]|:key[s]) %objects%",
                "%jsonelement% contains [:all] (:value[s]|:key[s]) %objects%",
                "%jsonelement% does(n't| not) contain [:all] (:value[s]|:key[s]) %objects%"
        );
        register.registerCondition(CondJsonPathExists.class,
                "%jsonelement% has [json] path %string%",
                "%jsonelement% does(n't| not) have [json] path %string%",
                "%jsonelement% contains [json] path %string%",
                "%jsonelement% does(n't| not) contain [json] path %string%"
        );
        register.registerCondition(CondJsonType.class,
                "json type of %jsonelement% is (json[ ](:object)|json[ ](:array)|json[ ](:primitive)|json[ ](:null))",
                "json type of %jsonelement% (is(n't| not)) (json[ ](:object)|json[ ](:array)|json[ ](:primitive)|json[ ](:null))",
                "%jsonelement% is [a] json[ ](:object|array|primitive|null)",
                "%jsonelement% is(n't| not) [a] json[ ](:object|array|primitive|null)"
        );

        register.registerCondition(CondJsonFileExist.class,
                "json file %string% exists",
                "json file %string% does(n't| not) exist",
                "file %string% exists as json",
                "file %string% does(n't| not) exist as json"
        );
        register.registerCondition(CondJsonIsEmpty.class,
                "json %jsonelement% is empty",
                "json %jsonelement% is(n't| not) empty",
                "%jsonelement% is empty json",
                "%jsonelement% is(n't| not) empty json"
        );
        register.registerCondition(CondIsCached.class,
                "[the] json storage [with [the] id] %string% is cached",
                "[the] json storage [with [the] id] %string% is(n't| not) cached",
                "json cache %string% exists",
                "json cache %string% does(n't| not) exist"
        );
        register.registerCondition(CondIsListened.class,
                "[the] json storage [with [the] id] %string% is listened",
                "[the] json storage [with [the] id] %string% is(n't| not) listened",
                "json cache %string% is (being )?watched",
                "json cache %string% is(n't| not) (being )?watched",
                "json file watcher [for] %string% is active",
                "json file watcher [for] %string% is(n't| not) active"
        );

        // ################ EFFECTS ############################
        register.registerEffect(EffNewFile.class,
                "new json file %~string%",
                "new json file %~string% with [content] %-objects%",
                "create [a] new json file %~string%",
                "create [a] new json file %~string% with [content] %-objects%"
        );
        register.registerEffect(EffMapJson.class,
                "[:async] (map|copy) %jsonelement% to %objects%",
                "[:async] store json %jsonelement% in %objects%",
                "[:async] copy json %jsonelement% to %objects%"
        );
        register.registerEffect(EffMergeJson.class,
                "merge %jsonelement% into %jsonelement%",
                "merge %jsonelement% into %jsonelement% deeply",
                "deeply merge %jsonelement% into %jsonelement%"
        );
        register.registerEffect(EffRemoveJsonPath.class,
                "remove [json] path %string% from %jsonelement%",
                "delete [json] path %string% in %jsonelement%",
                "delete value at path %string% in %jsonelement%"
        );
        register.registerEffect(EffNewFile.class,
                "create json file %string% [:with configuration<\\[\\s*((\\w+)=([\\w-]+)(?:,\\s*)?)+\\s*\\]>]",
                "create json file %string% and write to it %object% [:with configuration<\\[\\s*((\\w+)=([\\w-]+)(?:,\\s*)?)+\\s*\\]>]",
                "write json to file %string%",
                "write %object% to json file %string% [:with configuration<\\[\\s*((\\w+)=([\\w-]+)(?:,\\s*)?)+\\s*\\]>]"
        );

        // ################ CACHE ############################
        register.registerEffect(AEffHandleWatcher.class,
                "bind storage watcher to %string%",
                "unbind storage watcher from %string%",
                "watch json cache %string%",
                "stop watching json cache %string%"
        );
        register.registerEffect(EffVirtualStorage.class,
                "create json virtual storage named %string%",
                "create [a] virtual json cache (named|called|with id) %string%",
                "create [a] json cache named %string%"
        );
        register.registerEffect(AEffBindFile.class,
                "(bind|link) json file %string% as %string%",
                "(bind|link) json file %string% as %string% and let bind storage watcher",
                "watch json file %string% as cache %string%",
                "bind json file %string% to [json] cache %string%",
                "link json file %string% to cache %string% and watch [it]"
        );
        register.registerEffect(AEffUnbindFile.class,
                "un(bind|link) json storage [of] id %string%",
                "unbind json cache %string%",
                "unlink json cache %string%"
        );
        register.registerEffect(AEffSaveStorage.class,
                "save json cache %string%",
                "save json storage [with id] %string%",
                "save json storage id %string%",
                "save all json storages to disk",
                "save all json caches to disk"
        );

        // ################ EVENTS ############################
        register.registerEvent(
                "*Json file change", WatcherEvent.class, JsonFileChanged.class,
                "Runs when a watched JSON file or cache changes on disk.",
                "on json file change",
                "2.9, 5.5",
                "[json-] watcher file change",
                "[json-] watch save",
                "json cache update",
                "json file change"
        );

        register.registerEventValue(JsonFileChanged.class, JsonElement.class, JsonFileChanged::getJson,
                "event-json", "event-content", "changed json", "json from event");
        register.registerEventValue(JsonFileChanged.class, UUID.class, JsonFileChanged::getUuid,
                "event-uuid", "event-id", "watcher uuid");
        register.registerEventValue(JsonFileChanged.class, File.class, JsonFileChanged::getLinkedFile,
                "event-file", "event-link", "changed file", "json file");

        register.registerExpression(ExprJson.class, Object.class, ExpressionType.SIMPLE,
                "(:indexes|:indices|:keys|:values|:entries) (of|in) [json] (:array|:object) %jsonelement% [at path %-string%]"
        );
        register.registerExpression(ExprJsonLoop.class, Object.class, ExpressionType.SIMPLE,
                "[the] loop-(1¦key|2¦val|3¦iteration)[-%-*integer%]",
                "[the] json loop-(1¦key|2¦val|3¦iteration)[-%-*integer%]"
        );
    }
}
