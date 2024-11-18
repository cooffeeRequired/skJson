package cz.coffeerequired.skript.json;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.FileHandler;
import cz.coffeerequired.api.http.RequestClient;
import cz.coffeerequired.api.json.GsonParser;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.http.HttpResponse;
import java.util.Arrays;

public class ExprNewJson extends SimpleExpression<JsonElement> {

    private Expression<?> anyObjectExpression;
    private Expression<String> fileExpression;
    private Expression<String> websiteExpression;
    private Type currentTag;
    private FileType fileType;

    @Override
    protected @Nullable JsonElement[] get(Event event) {

        return switch (currentTag) {
            case ANY -> Arrays.stream(anyObjectExpression.getAll(event))
                    .map(GsonParser::toJson)
                    .toArray(JsonElement[]::new);
            case FILE -> switch (fileType) {
                case JSON -> {
                    String filePath = fileExpression.getSingle(event);
                    if (filePath == null) yield new JsonElement[0];

                    JsonElement jsonContent = FileHandler.get(new File(filePath)).join();
                    if (jsonContent == null) yield new JsonElement[0];

                    yield new JsonElement[]{jsonContent};
                }
                case UNKNOWN, YAML -> new JsonElement[0];
            };
            case WEBSITE -> {
                String website = websiteExpression.getSingle(event);
                if (website == null) yield new JsonElement[0];

                try (RequestClient client = new RequestClient()) {
                    HttpResponse<String> rsp = client
                            .setUri(website)
                            .method("GET")
                            .send();

                    if (rsp.statusCode() > 400) yield new JsonElement[0];

                    yield new JsonElement[]{GsonParser.toJson(rsp.body())};
                } catch (Exception e) {
                    SkJson.logger().exception(e.getMessage(), e);
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
    public String toString(@Nullable Event event, boolean b) {
        return "json from any sources";
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        currentTag = switch (i) {
            case 0 -> Type.FILE;
            case 1 -> Type.WEBSITE;
            default -> Type.ANY;
        };

        return switch (currentTag) {
            case FILE -> {
                fileExpression = LiteralUtils.defendExpression(expressions[0]);

                if (fileExpression.toString().contains(".json")) fileType = FileType.JSON;
                else if (fileExpression.toString().contains(".yaml")) fileType = FileType.YAML;
                else fileType = FileType.UNKNOWN;

                yield LiteralUtils.canInitSafely(fileExpression);
            }
            case ANY -> {
                anyObjectExpression = LiteralUtils.defendExpression(expressions[0]);
                yield LiteralUtils.canInitSafely(anyObjectExpression);
            }
            case WEBSITE -> {
                websiteExpression = LiteralUtils.defendExpression(expressions[0]);
                if (!websiteExpression.isSingle()) yield false;
                yield LiteralUtils.canInitSafely(websiteExpression);
            }
        };
    }

    private enum Type {ANY, FILE, WEBSITE}

    private enum FileType {UNKNOWN, YAML, JSON}
}
