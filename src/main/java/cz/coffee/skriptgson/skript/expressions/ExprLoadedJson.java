package cz.coffee.skriptgson.skript.expressions;

import ch.njol.skript.Skript;
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
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import static cz.coffee.skriptgson.SkriptGson.JSON_HASHMAP;
import static cz.coffee.skriptgson.util.Utils.newGson;


@Name("Load JSON as ID")
@Description({"Allows you to save the loaded Json as a custom ID"})
@Examples({"on script load:",
        "  set {_json} to loaded json \"Test\""
})
@Since("1.4.0")

public class ExprLoadedJson extends SimpleExpression<JsonElement> {

    private Expression<?> rawString;

    static {
        Skript.registerExpression(ExprLoadedJson.class, JsonElement.class, ExpressionType.COMBINED,
                "loaded json [id] %object%"
        );
    }

    @Override
    protected @Nullable JsonElement @NotNull [] get(@NotNull Event e) {
        JsonElement json;
        Object objectID = rawString.getSingle(e);
        if(objectID == null) return new JsonElement[0];
        String id = objectID.toString();
        json = newGson().toJsonTree(JSON_HASHMAP.get(id));
        if(json == null) return new JsonElement[0];
        return new JsonElement[]{json};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends JsonElement> getReturnType() {
        return JsonElement.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "loaded json " + rawString.toString(e, debug) + "";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        rawString = LiteralUtils.defendExpression(exprs[0]);
        return LiteralUtils.canInitSafely(rawString);
    }
}
