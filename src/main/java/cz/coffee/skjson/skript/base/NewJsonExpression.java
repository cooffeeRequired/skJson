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
import com.google.gson.*;
import cz.coffee.skjson.SkJson;
import cz.coffee.skjson.api.FileWrapper;
import cz.coffee.skjson.api.Update.HttpWrapper;
import cz.coffee.skjson.parser.ParserUtil;
import cz.coffee.skjson.skript.requests.Requests;
import cz.coffee.skjson.utils.Util;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static cz.coffee.skjson.api.Config.PROJECT_DEBUG;
import static cz.coffee.skjson.parser.ParserUtil.isClassicType;
import static cz.coffee.skjson.parser.ParserUtil.parse;

@Name("New json")
@Description({
        "latest:",
        "\tversion 2.9:",
                "\t\t- support now also multiple items as input",
                "\t\t- support json content from webpage",
                "\t\t- removed empty json array/object, cause it's not necessary while",
                  "skJson know parsing object",
        "original docs: https://skjsonteam.github.io/skJsonDocs/exprs#new-json",
        "skripthub docs:",
        "<br />",
        "It's allow create json from any source also from the file"
})
@Since("2.9")
@Examples({
        "on script load:",
            "\tset {_json} to json from json file \"plugins/Skript/json-storage/database.json\"",
            "\tset {_json::*} to json from \"{'test' :true}\", \"B\"",
            "\tset {_json} to json from diamond tools",
            "\tset {_json} to json from player's location",
            "\tset {_json} to json from player's inventory",
            "\tset {_json} to json from yaml file <path>",
            "\tset {_json} to json from website file \"https://json.org/sample.json\""
})

public class NewJsonExpression extends SimpleExpression<JsonElement> {

    static {
        SkJson.registerExpression(NewJsonExpression.class, JsonElement.class, ExpressionType.COMBINED,
                "json from [1:(text|string)|2:([json]|:yaml) file|3:web[site] [file]] [object] %objects%"
        );
    }

    private boolean isFile, isYaml, isWebFile, fileWithTab;
    private int mark;
    private Expression<?> input;
    private static final Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();


    @Override
    protected JsonElement @NotNull [] get(@NotNull Event e) {
        Object[] values = input.getAll(e);
        List<JsonElement> output = new ArrayList<>();
        if (isFile) {
            String stringifyFile = values[0].toString();
            if (stringifyFile != null) {
                final File file = new File(stringifyFile);
                output.add(Objects.requireNonNull(FileWrapper.fromNormal(file)).get());
            }
        } else if (isWebFile) {
            final Object url = input.getSingle(e);
            if (url == null) return new JsonElement[0];
            CompletableFuture<HttpWrapper.Response> ft = CompletableFuture.supplyAsync(() -> {
                HttpWrapper.Response rp = null;
                try (var client = new HttpWrapper(url.toString(), Requests.RequestMethods.GET)) {
                    rp = client
                            .addHeader("Content-Type", "application/json")
                            .request()
                            .process();
                } catch (Exception ex) {
                    Util.error(ex.getLocalizedMessage(), Objects.requireNonNull(getParser().getNode()));
                }

                return rp;
            });
            output.add((JsonElement) ft.join().getBodyContent(false));
        } else {
            for (Object value : values) {
                if (value instanceof JsonElement json) {
                    output.add(json);
                } else if (isClassicType(value)) {
                    JsonElement json;
                    try {
                        json = JsonParser.parseString(value.toString());
                    } catch (JsonParseException ex) {
                        json = gson.toJsonTree(value);
                    }
                    output.add(json);
                } else {
                    try {
                        if ((value instanceof ItemType type) && type.getTypes().size() > 1) {
                            type.getTypes().forEach(data -> output.add(ParserUtil.parse(data)));
                        } else {
                            output.add(parse(value));
                        }
                    } catch (Exception ex) {
                        if (PROJECT_DEBUG) Util.error(ex.getLocalizedMessage());
                    }
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
        return "json from " + switch (mark) {
            case 1 -> "text";
            case 2 -> isYaml ? "yaml file" : "json file";
            case 3 -> "website file";
            default -> "object";
        } + " " + input.toString(e, debug);
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, ParseResult parseResult) {
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
