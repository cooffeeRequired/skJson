package cz.coffee.skjson.skript.base;

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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import cz.coffee.skjson.SkJsonElements;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

import static cz.coffee.skjson.api.ColorWrapper.Colors.*;

@Name("Pretty print")
@Description({
        "Allows you to better parse json",
        "<pre>",
        "{",
        "\t\"test\": \"skJson\"",
        "\t\"Object\": {",
        "\t\t\"new\": \"data\"",
        "\t}",
        "</pre>"
})
@Since("2.9")
@Examples({
        "set {_json} to json from \"{'test': 'skJson', 'Object' : {'new': 'data'}}\"",
        "send {_json} with pretty print",
        "send {_json} with uncolored pretty print"
})

public class PrettyPrint extends SimpleExpression<String> {

    static final Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().setPrettyPrinting().create();
    private Expression<JsonElement> jElements;

    static {
        SkJsonElements.registerExpression(PrettyPrint.class, String.class, ExpressionType.SIMPLE,
                "%json% with [(:uncoloured|:uncolored)] pretty print");
    }

    private boolean uncoloured_;

    @Override
    protected @Nullable String @NotNull [] get(@NotNull Event e) {
        JsonElement[] jsons = jElements.getAll(e);
        ArrayList<String> coloredJsons = new ArrayList<>();

        if (uncoloured_) {
            Arrays.stream(jsons).forEach(json -> coloredJsons.add(gson.toJson(json)));
        } else {
            Arrays.stream(jsons).forEach(json -> coloredJsons.add(gson.toJson(json)
                    .replaceAll("(?<=\\\\W)([+]?([0-9]*[.])?[0-9]+)", AQUA.legacyColor + "$1" + WHITE.legacyColor)
                    .replaceAll("(?i:true)", GREEN.legacyColor + "$0" + WHITE.legacyColor)
                    .replaceAll("(?i:false)", RED.legacyColor + "$0" + WHITE.legacyColor)
                    .replaceAll("(\")((.)|)", DARK_GRAY.legacyColor + "$1" + WHITE.legacyColor + "$2" + WHITE.legacyColor)
                    .replaceAll("([{}])|([\\[\\]])", GRAY.legacyColor + "$1" + YELLOW.legacyColor + "$2" + WHITE.legacyColor)));
        }

        return coloredJsons.toArray(new String[0]);
    }

    @Override
    public boolean isSingle() {
        return jElements.isSingle();
    }

    @Override
    public @NotNull Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        if(e != null) {
            return jElements.toString(e, debug) + " with pretty print";
        } else {
            return "json with pretty print";
        }
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        jElements = LiteralUtils.defendExpression(exprs[0]);
        uncoloured_ = parseResult.hasTag("uncoloured") || parseResult.hasTag("uncolored");
        return LiteralUtils.canInitSafely(jElements);
    }
}
