package cz.coffee.skjson.skript.events.EventValues;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ExpressionType;

import java.io.File;

@Name("Watcher event value-expression File")
@Description("value-expression for getting file/link from current watcher event")
@Since("2.9")
public class EvtvFile extends EventValueExpression<File> {

    static {
        Skript.registerExpression(EvtvFile.class, File.class, ExpressionType.SIMPLE, "[the] [event-](file|link)");
    }

    public EvtvFile() {
        super(File.class);
    }
}
