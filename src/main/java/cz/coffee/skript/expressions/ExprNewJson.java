package cz.coffee.skript.expressions;

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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffee.core.utils.FileUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static cz.coffee.core.utils.AdapterUtils.parseItem;

@Name("New json")
@Description({
        "It's allow create json from any source also from the file"
})
@Examples({
        "set {_json} to json from file \"plugins/Skript/json-storage/database.json\"",
        "set {_json} to json from \"{\"\"test\"\":true}\"",
        "set {_json} to json from diamond sword",
        "set {_json} to json from player's location",
        "set {_json} to json from player's inventory",
        "set {_json} to json from yaml file \"string\""
})
@Since("2.8.0 - performance & clean")


public class ExprNewJson extends SimpleExpression<JsonElement> {
    static {
        Skript.registerExpression(ExprNewJson.class, JsonElement.class, ExpressionType.COMBINED,
                "json[s] from [text|string] %object%",
                "json from [(json|:yaml)] file %string%",
                "[empty] json(-| )(0:array|1:object)"
        );
    }

    private Expression<?> objects;
    private Expression<String> file;
    private boolean isFile, isArray, isYAML;
    private int line;


    @Override
    protected @Nullable JsonElement @NotNull [] get(@NotNull Event e) {
        if (isFile) {
            String fileString = this.file.getSingle(e);
            if (fileString == null) return new JsonElement[0];
            if (isYAML)
                return new JsonElement[]{FileUtils.getFromYaml(new File(fileString))};
            return new JsonElement[]{FileUtils.get(new File(fileString))};
        } else if (line == 2) {
            if (isArray) return new JsonElement[]{new JsonArray()};
            else return new JsonElement[]{new JsonObject()};
        } else {
            Object object = this.objects.getSingle(e);
            return new JsonElement[]{parseItem(object, this.objects, e)};
        }
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
        if (line == 0) {
            return "json from (text|string) " + objects.toString(e, debug);
        } else if (line == 1) {
            return "json from file " + file.toString(e, debug);
        } else {
            return "empty json " + (isArray ? "array" : "object");
        }
    }

    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        isFile = matchedPattern == 1;
        line = matchedPattern;
        isArray = parseResult.mark == 0;
        isYAML = parseResult.hasTag("yaml");
        if (line == 2) return true;
        objects = LiteralUtils.defendExpression(exprs[0]);
        if (isFile) {
            file = (Expression<String>) exprs[0];
        }
        return LiteralUtils.canInitSafely(objects);
    }

}
