package cz.coffee.skjson.skript.base;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import cz.coffee.skjson.SkJsonElements;
import cz.coffee.skjson.api.FileHandler;
import cz.coffee.skjson.api.http.RequestClient;
import cz.coffee.skjson.api.http.RequestResponse;
import cz.coffee.skjson.parser.ParserUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;

import static cz.coffee.skjson.api.ConfigRecords.PROJECT_DEBUG;
import static cz.coffee.skjson.parser.ParserUtil.parse;
import static cz.coffee.skjson.utils.Logger.error;
import static cz.coffee.skjson.utils.Logger.warn;

@SuppressWarnings("unused")
@Name("New json")
@Description({
    "latest:",
    "\t\t- support now also multiple items as input",
    "\t\t- support json content from webpage",
    "\t\t- removed empty json array/object, cause it's not necessary while",
    "skJson know parsing object",
    "<br />",
    "It's allow create json from any source also from the file"
})
@Since("2.9, 2.9.3 - Literal parsing, 3.1.0 - Request checks fix, 4.0 - remove literals.")
@Examples({
    "on script load:",
    "\tset {_json} to json from json file \"plugins/Skript/json-storage/database.json\"",
    "\tset {_json::*} to json from \"{'test' :true}\", \"B\"",
    "\tset {_json} to json from diamond tools",
    "\tset {_json} to json from player's location",
    "\tset {_json} to json from player's inventory",
    "\tset {_json} to json from yaml file <path>",
    "\tset {_json} to json from website file \"https://json.org/sample.json\"",
})

public class NewJsonExpression extends SimpleExpression<JsonElement> {
    static {
        SkJsonElements.registerExpression(NewJsonExpression.class, JsonElement.class, ExpressionType.COMBINED,
            "json from [1:(text|string)|2:([json]|:yaml) file|3:web[site] [file]] [object] %objects%"
        );
    }

    private boolean isFile, isYaml, isWebFile;
    private int mark;
    private Expression<?> input;

    @SuppressWarnings("DataFlowIssue")
    private File sanitizedFile(String file) {
        if (file.startsWith("~")) {
            return new File(Bukkit.getPluginManager().getPlugin("Skript").getDataFolder(), "scripts" + "/" + file.substring(1));
        }
        return new File(file);
    }

    @Override
    protected JsonElement @NotNull [] get(@NotNull Event e) {
        List<JsonElement> output = new ArrayList<>();
        Object[] values = input.getAll(e);
        if (isFile) {
            String stringifyFile = values[0].toString();
            if (stringifyFile != null) {
                final File file = sanitizedFile(stringifyFile);

                // make a sensitization for Failed get from FileWrapper
                JsonElement json = FileHandler.get(file).join();
                if (json == null) {
                    output.add(JsonParser.parseString("{Error: 'File does not exist! Or File is corrupted! " + stringifyFile + "'}"));
                } else {
                    output.add(json);
                }
            }
        } else if (isWebFile) {
            final Object url = input.getSingle(e);
            if (url == null) return new JsonElement[0];
            CompletableFuture<RequestResponse> ft = CompletableFuture.supplyAsync(() -> {
                RequestResponse rp = null;
                try (var client = new RequestClient(url.toString())) {
                    rp = client
                        .method("GET")
                        .addHeaders(new WeakHashMap<>(Map.of("Content-Type", "application/json")))
                        .request().join();
                } catch (Exception ex) {
                    error(ex, Bukkit.getConsoleSender(), getParser().getNode());
                }
                return rp;
            });
            RequestResponse joined = ft.join();
            if (joined != null && joined.isSuccessfully()) {
                JsonElement element = (JsonElement) joined.getBodyContent(false);
                output.add(element);
            } else {
                warn("You cannot get non-json content via this.");
                output.add(JsonNull.INSTANCE);
            }
        } else {
            for (Object value : values) {
                try {
                    if ((value instanceof ItemType type) && type.getTypes().size() > 1) {
                        type.getTypes().forEach(data -> output.add(ParserUtil.parse(data)));
                    } else {
                        output.add(parse(value));
                    }
                } catch (Exception ex) {
                    if (PROJECT_DEBUG) error(ex, null, getParser().getNode());
                }
            }
        }
        return output.toArray(JsonElement[]::new);
    }

    @Override
    public boolean isSingle() {
        return input.isSingle();
    }

    @Override
    public @NotNull Class<? extends JsonElement> getReturnType() {
        return JsonElement.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        try {
            assert e != null;
            return "json from " + switch (mark) {
                case 1 -> "text";
                case 2 -> isYaml ? "yaml file" : "json file";
                case 3 -> "website file";
                default -> "object";
            } + " " + (this.input != null ? this.input.toString(e, debug) : "");
        } catch (Exception ex) {
            error(ex, null, getParser().getNode());
        }
        return "";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed,
                        ParseResult parseResult) {
        mark = parseResult.mark;
        isFile = mark == 2;
        isWebFile = mark == 3;
        isYaml = (isFile && parseResult.hasTag("yaml"));
        input = LiteralUtils.defendExpression(exprs[0]);
        if (isWebFile || isFile) {
            if (!input.isSingle()) {
                return false;
            }
        }
        return LiteralUtils.canInitSafely(input);
    }
}
