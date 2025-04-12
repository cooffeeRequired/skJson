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
import cz.coffeerequired.api.SkJsonLogger;
import cz.coffeerequired.api.json.GsonParser;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Pretty print")
@Description("Allows you print out the json with formatted format, 4 tab padding and colored for some elements")
@Since("2.9")
@Examples({
        "set {_json} to json from \"{'test': 'skJson', 'Object' : {'new': 'data'}}\"",
        "send {_json} as pretty print",
        "send {_json} as uncolored pretty print"
})
public class ExprPrettyPrint extends SimpleExpression<String> {

    private Format format;
    private Expression<JsonElement> element;

    @Override
    protected @Nullable String[] get(Event event) {
        JsonElement json = element.getSingle(event);
        if (json == null) return null;

        String defaultStringifyJson = GsonParser.toPrettyPrintString(json);

        if (format.equals(Format.PRETTY)) {
            defaultStringifyJson = defaultStringifyJson
                    .replaceAll("(\\s+\".*?\")", "&2$1&7")
                    .replaceAll("(?i:true)", "&2$0&f") // Green for 'true'
                    .replaceAll("(?i:false)", "&4$0&f") // Red for 'false'
                    .replaceAll("(?i:null)", "&5$0&f") // Purple for 'null'
                    .replaceAll("[{}]", "&7$0&f")
                    .replaceAll("[\\[\\]]", "&6$0&f");
        }
        return new String[]{SkJsonLogger.translate(defaultStringifyJson)};
    }

    @Override
    public boolean isSingle() {
        return this.element.isSingle();
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "json as pretty printed " + (this.format == Format.PRETTY ? "string" : "uncolored string");
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        if (i == 0) format = Format.PRETTY;
        else if (i == 1) format = Format.UNCOLORED;
        element = LiteralUtils.defendExpression(expressions[0]);
        return LiteralUtils.canInitSafely(element);
    }

    private enum Format {
        PRETTY,
        UNCOLORED
    }
}
