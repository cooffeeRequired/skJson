package cz.coffeerequired.skript.core;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.google.gson.JsonObject;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.support.Performance;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.io.File;

@Name("Virtual json storage")
@Description("Allows create an virtual instance of json file-storage, without physical file.")
@Examples("""
    create json virtual storage named "my-virtual-memory-storage"
""")
@Since("2.9")
public class EffVirtualStorage extends Effect {

    private Expression<String> expressionName;

    @Override
    protected void execute(Event event) {
        var perf = new Performance();
        String name = expressionName.getSingle(event);
        if (name == null) {
            SkJson.severe("You must specify a name for the virtual storage.");
            return;
        }

        if (Api.getCache().containsKey(name)) {
            perf.stop();
            SkJson.info("Creating virtual memory cache: " + perf.toHumanTime());
            return;
        }

        Api.getCache().addValue(name, new JsonObject(), new File("Undefined"));
        perf.stop();
        SkJson.info("Creating virtual  memory cache: " + perf.toHumanTime());
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "create virtual json storage named " + expressionName.toString(event, debug);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        expressionName = (Expression<String>) expressions[0];
        return expressionName != null;
    }
}
