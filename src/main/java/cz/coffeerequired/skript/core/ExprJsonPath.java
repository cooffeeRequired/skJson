package cz.coffeerequired.skript.core;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.json.JsonPath;
import cz.coffeerequired.api.json.SerializedJson;
import cz.coffeerequired.api.json.SkriptJsonInputParser;
import org.bukkit.event.Event;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.Nullable;

// json path %string% in %json%

public class ExprJsonPath extends SimpleExpression<JsonPath> {

    private Expression<JsonElement> exprJson;
    private Expression<String> exprPath;

    @Override
    protected @Nullable JsonPath[] get(Event event) {
        JsonElement jsonElement = exprJson.getSingle(event);
        if (jsonElement == null) return new JsonPath[0];
        String path = exprPath.getSingle(event);
        var tokens = SkriptJsonInputParser.tokenize(path, Api.Records.PROJECT_DELIM);
        return new JsonPath[] {new JsonPath(jsonElement, path, tokens)};
    }

    @Override
    public boolean isSingle() {
        return true ;
    }

    @Override
    public Class<? extends JsonPath> getReturnType() {
        return JsonPath.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "json path " + exprPath.toString(event, b) + " in " + exprJson.toString(event, b);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        exprJson = LiteralUtils.defendExpression(expressions[1]);
        exprPath = (Expression<String>) expressions[0];
        return exprPath != null && LiteralUtils.canInitSafely(exprPath);
    }
}
