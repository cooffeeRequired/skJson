package cz.coffeerequired.skript.core.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.FileHandler;
import cz.coffeerequired.api.http.RequestClient;
import cz.coffeerequired.api.json.Parser;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Name("New json element")
@Description({
        "Creates a JsonElement from a string, Bukkit object, file, or URL.",
        "Supported sources include items, locations, inventories, players, and other registered types.",
        "Files under `~` are resolved relative to the Skript scripts folder.",
        "Prefer `parse … as json` for JSON strings."
})
@Since("4.1 - API UPDATE")
@Examples({
        "on script load:",
        "\tset {_json} to json from file \"plugins/SkJson/skjson/homes.json\"",
        "\tset {_json} to parse \"{\\\"test\\\": true}\" as json",
        "\tset {_json} to json from player's location",
        "\tset {_json} to json from player's inventory",
        "\tset {_json} to json from url \"https://json.org/sample.json\"",
})
public class ExprNewJson extends SimpleExpression<JsonElement> {

    private Expression<?> anyObjectExpression;
    private Expression<String> fileExpression;
    private Expression<String> websiteExpression;
    private Type currentTag;
    private FileType fileType;

    @Override
    protected @Nullable JsonElement[] get(Event event) {
        return switch (currentTag) {
            case ANY -> {
                Object[] values = anyObjectExpression.getArray(event);
                if (values == null) {
                    yield new JsonElement[0];
                }
                yield Arrays.stream(values).map(Parser::toJson).toArray(JsonElement[]::new);
            }
            case FILE -> switch (fileType) {
                case JSON -> {
                    String filePath = fileExpression.getSingle(event);
                    if (filePath == null) {
                        yield new JsonElement[0];
                    }

                    if (filePath.startsWith("~")) {
                        filePath = Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("Skript"))
                                .getDataFolder().getPath() + "/scripts/" + filePath.substring(1);
                    }

                    JsonElement jsonContent = FileHandler.get(new File(filePath)).join();
                    if (jsonContent == null) {
                        yield new JsonElement[0];
                    }
                    yield new JsonElement[]{jsonContent};
                }
                case UNKNOWN, YAML -> new JsonElement[0];
            };
            case WEBSITE -> {
                String website = websiteExpression.getSingle(event);
                if (website == null) {
                    yield new JsonElement[0];
                }

                try (RequestClient client = new RequestClient()) {
                    HttpResponse<String> rsp = client
                            .setUri(website)
                            .method("GET")
                            .send();

                    if (rsp.statusCode() > 400) {
                        yield new JsonElement[0];
                    }
                    yield new JsonElement[]{Parser.toJson(rsp.body())};
                } catch (Exception e) {
                    SkJson.exception(e, e.getMessage());
                    yield new JsonElement[0];
                }
            }
        };
    }

    @Override
    public boolean isSingle() {
        return switch (currentTag) {
            case ANY -> anyObjectExpression.isSingle();
            case FILE, WEBSITE -> true;
        };
    }

    @Override
    public Class<? extends JsonElement> getReturnType() {
        return JsonElement.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "json from any sources";
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        currentTag = switch (matchedPattern) {
            case 0, 1, 2 -> Type.FILE;
            case 3, 4, 5 -> Type.WEBSITE;
            default -> Type.ANY;
        };

        return switch (currentTag) {
            case FILE -> {
                fileExpression = LiteralUtils.defendExpression(expressions[0]);
                String sample = fileExpression.toString();
                if (sample.contains(".json") || sample.contains(".jsonc")) {
                    fileType = FileType.JSON;
                } else if (sample.contains(".yaml")) {
                    fileType = FileType.YAML;
                } else {
                    fileType = FileType.UNKNOWN;
                }
                yield LiteralUtils.canInitSafely(fileExpression);
            }
            case ANY -> {
                anyObjectExpression = LiteralUtils.defendExpression(expressions[0]);
                yield LiteralUtils.canInitSafely(anyObjectExpression);
            }
            case WEBSITE -> {
                websiteExpression = LiteralUtils.defendExpression(expressions[0]);
                if (!websiteExpression.isSingle()) {
                    yield false;
                }
                yield LiteralUtils.canInitSafely(websiteExpression);
            }
        };
    }

    private enum Type {ANY, FILE, WEBSITE}

    private enum FileType {UNKNOWN, YAML, JSON}
}
