package cz.coffeerequired.skript.core.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import cz.coffeerequired.api.FileHandler;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.io.File;

@Name("File properties")
@Description({
        "You can get file name, path, size and content"
})
@Examples("""
        loop json files from dir ".":
            send loop-file's name without file type
            send loop-file's path
            send loop-file's size
            send loop-file's content
        """)
@Since("5.4")
public class propExprJsonFile extends PropertyExpression<File, Object> {
    private int mark;
    private boolean withoutExtension;

    @Override
    protected Object[] get(Event event, File[] source) {
        Object source_ = source[0];
        if (source_ instanceof File current ) {
            return switch (this.mark) {
                case 1 -> new Object[]{withoutExtension ? current.getName().replaceAll("\\.(.*)", "") : current.getName()};
                case 2 -> new Object[]{current.getPath()};
                case 3 -> new Object[]{current.length()};
                case 4 -> new Object[]{FileHandler.get(current).join()};
                default -> new Object[]{current};
            };
        }
        return null;
    }

    @Override
    public Class<?> getReturnType() {
        return Object.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "sdasdasd.ads.";
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.mark = parseResult.mark;
        this.withoutExtension = parseResult.hasTag("without file type");
        setExpr((Expression<? extends File>) expressions[0]);
        return true;
    }
}
