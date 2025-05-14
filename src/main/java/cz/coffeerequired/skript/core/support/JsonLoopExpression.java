package cz.coffeerequired.skript.core.support;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.sections.SecLoop;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.json.JsonAccessorUtils;
import cz.coffeerequired.api.json.Parser;
import cz.coffeerequired.skript.core.expressions.ExprJsonValues;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cz.coffeerequired.api.Api.Records.PROJECT_DEBUG;

@Name("Json loop")
@Description("""
            That will allow loop through json, and get key/index or value
            **json-value**, **json-key**
            """)
@Since("4.1 - API UPDATE")
@Examples("""
            set {_json} to json from "{test: [true, false, {A: [1,8,3]}]}"
            loop values "test::2" of {_json}:
                send "&eLOOP: %json-key%: %json-value%"
                loop values of json-value:
                    send "&bLOOP 2: %json-key-2%: %json-value-2%"
            """)

public class JsonLoopExpression extends SimpleExpression<Object> {

    private boolean isKey;
    private SecLoop loop;
    private boolean isCanceled = false;
    private static final Pattern INPUT_PATTERN = Pattern.compile("json-(.+)(.)");


    @SuppressWarnings("unchecked")
    private Object[] getCurrent(Event event) {
        try {
            Map<String, Object> out = (Map<String, Object>) loop.getCurrent(event);
            if (out == null || out.isEmpty()) {
                return new Object[0];
            }
            
            Map.Entry<String, Object> entry = out.entrySet().iterator().next();
            if (isKey) {
                return new String[]{entry.getKey()};
            }
            
            Object value = entry.getValue();
            if (value instanceof JsonElement element) {
                Object converted = Parser.fromJson(element);
                return new Object[]{converted != null ? converted : Parser.fromJson(element)};
            }
            return new Object[]{value};
        } catch (ClassCastException exception) {
            if (PROJECT_DEBUG) SkJson.exception(exception, exception.getLocalizedMessage());
            return new Object[0];
        }
    }

    @Override
    protected @Nullable Object[] get(Event event) {
        return getCurrent(event);
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<?> getReturnType() {
        if (loop == null) return Object.class;
        return loop.getLoopedExpression().getReturnType();
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return Classes.getDebugMessage(loop.getCurrent(event));
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull SkriptParser.ParseResult parseResult) {
        isKey = parseResult.hasTag("key");
        String firstField = parseResult.expr;
        
        int loopIndex = 0;
        String loopType = "";
        
        Matcher matcher = INPUT_PATTERN.matcher(firstField);
        if (matcher.matches()) {
            String[] parts = firstField.split("-");
            loopType = parts[1];
            MatchResult numberOfLoop = !parseResult.regexes.isEmpty() ? parseResult.regexes.getFirst() : null;
            if (numberOfLoop != null) {
                loopIndex = Objects.requireNonNull(JsonAccessorUtils.isNumeric(numberOfLoop.group(0))).intValue();
            }
        }

        SecLoop targetLoop = null;
        int currentIndex = 1;
        
        for (SecLoop l : getParser().getCurrentSections(SecLoop.class)) {
            if (!l.getLoopedExpression().isLoopOf("skjson-custom-loop")) {
                continue;
            }
            
            ((ExprJsonValues) l.getLoopedExpression()).relevantToLoop = true;
            
            if (currentIndex < loopIndex) {
                currentIndex++;
                continue;
            }
            
            if (targetLoop != null) {
                isCanceled = true;
                break;
            }
            
            targetLoop = l;
            if (currentIndex == loopIndex) {
                break;
            }
        }

        if (targetLoop == null) {
            Skript.error("There's no loop that matches 'json-" + loopType + " " + loopIndex + "'", ErrorQuality.SEMANTIC_ERROR);
            return false;
        }
        
        if (isCanceled) {
            Skript.error("There are multiple loops that match json-" + loopType + ". Use json-" + loopType + "-1/2/3/etc. to specify which loop's value you want.", ErrorQuality.SEMANTIC_ERROR);
            return false;
        }
        
        this.loop = targetLoop;
        return true;
    }

    public boolean isLoopOf(@NotNull String s) {
        return false;
    }
}