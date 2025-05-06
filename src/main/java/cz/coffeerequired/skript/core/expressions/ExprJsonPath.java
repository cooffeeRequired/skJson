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
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.json.SkriptJsonInputParser;
import cz.coffeerequired.api.types.JsonPath;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unchecked")
@Name("Get Json Path")
@Description("Returns the json path from the given json element. The path must be a valid json path.")
@Examples("""
        set {_json} to json from "{array: [{A: 1, B: 2, C: 3}]}"
        send json path "array.0" in {_json}
        send json path "array.0.A" in {_json}
        
        
        set {_json} to json from "{}"
        set value of json path "r[0].A" in {_json} to "classic set - array - nested"
        set value of json path "r[0].B" in {_json} to "classic set - array - nested"
        set value of json path "r[1].c" in {_json} to "troll"
        set value of json path "r[1].d" in {_json} to "troll 2"
        set value of json path "r[1].e[0].x.b" in {_json} to "troll 3"
        set value of json path "r[0].X.e" in {_json} to "troll 3"
        
        
        """)
@Since("5.0")
public class ExprJsonPath extends SimpleExpression<JsonPath> {

    private Expression<JsonElement> exprJson;
    private Expression<String> exprPath;

    @Override
    protected @Nullable JsonPath[] get(Event event) {
        JsonElement jsonElement = exprJson.getSingle(event);
        if (jsonElement == null) return new JsonPath[0];
        String path = exprPath.getSingle(event);
        var tokens = SkriptJsonInputParser.tokenize(path, Api.Records.PROJECT_DELIM);
        return new JsonPath[]{new JsonPath(jsonElement, path, tokens)};
    }

    @Override
    public boolean isSingle() {
        return true;
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
