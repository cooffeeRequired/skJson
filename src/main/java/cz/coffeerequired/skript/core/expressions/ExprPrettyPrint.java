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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import cz.coffeerequired.api.json.Parser;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;

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

    public String colorizeJson(JsonElement element) {
        StringBuilder sb = new StringBuilder();
        colorizeJsonRecursive(element, sb, 0);
        return sb.toString();
    }

    private void colorizeJsonRecursive(JsonElement element, StringBuilder sb, int indent) {
        String indentStr = "  ".repeat(indent);

        if (element.isJsonObject()) {
            sb.append("§7{§f\n");
            JsonObject obj = element.getAsJsonObject();
            Iterator<Map.Entry<String, JsonElement>> iter = obj.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, JsonElement> entry = iter.next();
                sb.append(indentStr).append("  ").append("§2\"").append(entry.getKey()).append("\"§f: ");
                colorizeJsonRecursive(entry.getValue(), sb, indent + 1);
                if (iter.hasNext()) sb.append(",");
                sb.append("\n");
            }
            sb.append(indentStr).append("§7}§f");
        } else if (element.isJsonArray()) {
            sb.append("§6[§f\n");
            JsonArray arr = element.getAsJsonArray();
            for (int i = 0; i < arr.size(); i++) {
                sb.append(indentStr).append("  ");
                colorizeJsonRecursive(arr.get(i), sb, indent + 1);
                if (i < arr.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append(indentStr).append("§6]§f");
        } else if (element.isJsonNull()) {
            sb.append("§5null§f");
        } else if (element.isJsonPrimitive()) {
            JsonPrimitive prim = element.getAsJsonPrimitive();
            if (prim.isBoolean()) {
                sb.append(prim.getAsBoolean() ? "§2true§f" : "§4false§f");
            } else if (prim.isNumber()) {
                sb.append("§9").append(prim.getAsNumber()).append("§f");
            } else if (prim.isString()) {
                sb.append("§2\"").append(prim.getAsString()).append("\"§f");
            }
        }
    }


    @Override
    protected @Nullable String[] get(Event event) {
        JsonElement json = element.getSingle(event);
        if (json == null) return null;
        return new String[]{this.format.equals(Format.PRETTY) ? colorizeJson(json) : Parser.getGson().toJson(json)};
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
