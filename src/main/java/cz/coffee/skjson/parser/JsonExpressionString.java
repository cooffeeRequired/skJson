package cz.coffee.skjson.parser;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.ConvertedExpression;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.BlockingLogHandler;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.structures.StructVariables.DefaultVariables;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.chat.ChatMessages;
import ch.njol.skript.util.chat.MessageComponent;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.SingleItemIterator;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a string that may contain expressions, and is thus "variable".
 */
public class JsonExpressionString implements Expression<String> {

    @Nullable
    private final Script script;
    private final String orig;

    @Nullable
    private final Object[] string;

    private final boolean isSimple;

    @Nullable
    private final String simple;

    @Nullable
    private final String simpleUnformatted;
    private final StringMode mode;

    /**
     * Creates a new VariableString which does not contain variables.
     *
     * @param input Content for string.
     */
    private JsonExpressionString(String input) {
        this.isSimple = true;
        this.simpleUnformatted = input.replace("%%", "%"); // This doesn't contain variables, so this wasn't done in newInstance!
        this.simple = Utils.replaceChatStyles(simpleUnformatted);

        this.orig = simple;
        this.string = null;
        this.mode = StringMode.MESSAGE;

        ParserInstance parser = getParser();
        this.script = parser.isActive() ? parser.getCurrentScript() : null;

    }

    /**
     * Creates a new VariableString which contains variables.
     *
     * @param orig   Original string (unparsed).
     * @param string Objects, some of them are variables.
     * @param mode   String mode.
     */
    @SuppressWarnings("all")
    private JsonExpressionString(String orig, Object[] string, StringMode mode) {
        this.orig = orig;
        this.string = new Object[string.length];
        @Nullable Object[] stringUnformatted = new Object[string.length];

        ParserInstance parser = getParser();
        this.script = parser.isActive() ? parser.getCurrentScript() : null;

        // Construct unformatted string and components
        List<MessageComponent> components = new ArrayList<>(string.length);
        for (int i = 0; i < string.length; i++) {
            Object object = string[i];
            if (object instanceof String) {
                this.string[i] = Utils.replaceChatStyles((String) object);
                components.addAll(ChatMessages.parse((String) object));
            } else {
                this.string[i] = object;
                components.add(null); // Not known parse-time
            }

            // For unformatted string, don't format stuff
            stringUnformatted[i] = object;
        }
        components.toArray(new MessageComponent[0]);
        this.mode = mode;

        this.isSimple = false;
        this.simple = null;
        this.simpleUnformatted = null;
    }

    /**
     * Prints errors
     */
    @Nullable
    public static JsonExpressionString newInstance(String input) {
        return newInstance(input, StringMode.MESSAGE);
    }

    /**
     * Creates an instance of VariableString by parsing given string.
     * Prints errors and returns null if it is somehow invalid.
     *
     * @param orig Unquoted string to parse.
     * @return A new VariableString instance.
     */
    @Nullable
    public static JsonExpressionString newInstance(String orig, StringMode mode) {
        if (mode != StringMode.VARIABLE_NAME && !isQuotedCorrectly(orig, false))
            return null;
        int n = StringUtils.count(orig, '%');
        if (n % 2 != 0) {
            Skript.error("The percent sign is used for expressions (e.g. %player%). To insert a '%' type it twice: %%.");
            return null;
        }

        String s = getString(orig, mode);

        List<Object> string = new ArrayList<>(n / 2 + 2); // List of strings and expressions
        int c = s.indexOf('%');
        if (c != -1) {
            if (c != 0)
                string.add(s.substring(0, c));
            while (c != s.length()) {
                int c2 = s.indexOf('%', c + 1);

                int a = c;
                int b;
                while (c2 != -1 && (b = s.indexOf('{', a + 1)) != -1 && b < c2) {
                    a = nextVariableBracket(s, b + 1);
                    if (a == -1) {
                        Skript.error("Missing closing bracket '}' to end variable");
                        return null;
                    }
                    c2 = s.indexOf('%', a + 1);
                }
                if (c2 == -1) {
                    assert false;
                    return null;
                }
                if (c + 1 == c2) {
                    // %% escaped -> one % in result string
                    if (!string.isEmpty() && string.get(string.size() - 1) instanceof String) {
                        string.set(string.size() - 1, string.get(string.size() - 1) + "%");
                    } else {
                        string.add("%");
                    }
                } else {
                    RetainingLogHandler log = SkriptLogger.startRetainingLog();
                    try {
                        Expression<?> expr =
                                new SkriptParser(s.substring(c + 1, c2), SkriptParser.PARSE_EXPRESSIONS, ParseContext.DEFAULT)
                                        .parseExpression(Object.class);
                        if (expr == null) {
                            log.printErrors("Can't understand this expression: " + s.substring(c + 1, c2));
                            return null;
                        } else {
                            string.add(expr);
                        }
                        log.printLog();
                    } finally {
                        log.stop();
                    }
                }
                c = s.indexOf('%', c2 + 1);
                if (c == -1)
                    c = s.length();
                String l = s.substring(c2 + 1, c); // Try to get string (non-variable) part
                if (!l.isEmpty()) { // This is string part (no variables)
                    if (!string.isEmpty() && string.get(string.size() - 1) instanceof String) {
                        // We can append last string part in the list, so let's do so
                        string.set(string.size() - 1, string.get(string.size() - 1) + l);
                    } else { // Can't append, just add new part
                        string.add(l);
                    }
                }
            }
        } else {
            // Only one string, no variable parts
            string.add(s);
        }
        // Check if this isn't actually variable string, and return
        if (string.size() == 1 && string.get(0) instanceof String) {
            return new JsonExpressionString(s);
        }

        Object[] sa = string.toArray();
        if (string.size() == 1 && string.get(0) instanceof Expression &&
                ((Expression<?>) string.get(0)).getReturnType() == String.class &&
                ((Expression<?>) string.get(0)).isSingle() &&
                mode == StringMode.MESSAGE) {
            String expr = ((Expression<?>) string.get(0)).toString(null, false);
            Skript.warning(expr + " is already a text, so you should not put it in one (e.g. " + expr + " instead of " + "\"%" + expr.replace("\"", "\"\"") + "%\")");
        }
        return new JsonExpressionString(orig, sa, mode);
    }

    private static String getString(String orig, StringMode mode) {
        String s;
        if (mode != StringMode.VARIABLE_NAME) {
            // Replace every double " character with a single ", except for those in expressions (between %)
            StringBuilder stringBuilder = new StringBuilder();

            boolean expression = false;
            for (int i = 0; i < orig.length(); i++) {
                char c = orig.charAt(i);
                stringBuilder.append(c);

                if (c == '%')
                    expression = !expression;

                if (!expression && c == '"')
                    i++;
            }
            s = stringBuilder.toString();
        } else {
            s = orig;
        }
        return s;
    }

    /**
     * Tests whether a string is correctly quoted, i.e. only has doubled double quotes in it.
     * Singular double quotes are only allowed between percentage signs.
     *
     * @param s          The string
     * @param withQuotes Whether s must be surrounded by double quotes or not
     * @return Whether the string is quoted correctly
     */
    public static boolean isQuotedCorrectly(String s, boolean withQuotes) {
        if (withQuotes && (!s.startsWith("\"") || !s.endsWith("\"") || s.length() < 2))
            return false;
        boolean quote = false;
        boolean percentage = false;
        if (withQuotes)
            s = s.substring(1, s.length() - 1);
        for (char c : s.toCharArray()) {
            if (percentage) {
                if (c == '%')
                    percentage = false;
                continue;
            }
            if (quote && c != '"')
                return false;
            if (c == '"') {
                quote = !quote;
            } else if (c == '%') {
                percentage = true;
            }
        }
        return !quote;
    }

    /**
     * Copied from {@code SkriptParser#nextBracket(String, char, char, int, boolean)}, but removed escaping & returns -1 on error.
     *
     * @param start Index after the opening bracket
     * @return The next closing curly bracket
     */
    public static int nextVariableBracket(String s, int start) {
        int n = 0;
        for (int i = start; i < s.length(); i++) {
            if (s.charAt(i) == '}') {
                if (n == 0)
                    return i;
                n--;
            } else if (s.charAt(i) == '{') {
                n++;
            }
        }
        return -1;
    }

    @Override
    public @NotNull String toString() {
        return toString(null, false);
    }

    /**
     * Parses all expressions in the string and returns it.
     * If this is a simple string, the event may be null.
     *
     * @param event Event to pass to the expressions.
     * @return The input string with all expressions replaced.
     */

    @SuppressWarnings("all")
    public String toString(@Nullable Event event) {
        if (isSimple) {
            assert simple != null;
            return simple;
        }
        if (event == null)
            throw new IllegalArgumentException("Event may not be null in non-simple VariableStrings!");

        Object[] string = this.string;
        assert string != null;
        StringBuilder builder = new StringBuilder();
        List<Class<?>> types = new ArrayList<>();
        for (Object object : string) {
            if (object instanceof Expression<?>) {
                Object[] objects = ((Expression<?>) object).getArray(event);
                if (objects.length > 0)
                    types.add(objects[0].getClass());

                if (((Expression<?>) object).getReturnType().equals(String.class)) {
                    for (Object o : objects) {
                        if (o instanceof String str) {
                            builder.append(Classes.toString("\"" + str + "\"", mode));
                        } else {
                            builder.append(Classes.toString(object, mode));
                        }
                    }
                } else {
                    builder.append(Classes.toString(objects, true, mode));
                }
            } else {
                builder.append(object);
            }
        }
        String complete = builder.toString();
        if (script != null && mode == StringMode.VARIABLE_NAME && !types.isEmpty()) {
            DefaultVariables data = script.getData(DefaultVariables.class);
            if (data != null)
                data.add(complete, types.toArray(new Class<?>[0]));
        }
        return complete;
    }

    /**
     * Use {@link #toString(Event)} to get the actual string. This method is for debugging.
     */
    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        if (isSimple) {
            assert simple != null;
            return '"' + simple + '"';
        }
        Object[] string = this.string;
        assert string != null;
        StringBuilder builder = new StringBuilder("\"");
        for (Object object : string) {
            if (object instanceof Expression) {
                builder.append("%").append(((Expression<?>) object).toString(event, debug)).append("%");
            } else {
                builder.append(object);
            }
        }
        builder.append('"');
        return builder.toString();
    }

    public boolean isSimple() {
        return isSimple;
    }

    public StringMode getMode() {
        return mode;
    }

    public JsonExpressionString setMode(StringMode mode) {
        if (this.mode == mode || isSimple)
            return this;
        @SuppressWarnings("resource")
        BlockingLogHandler h = new BlockingLogHandler().start();
        try {
            JsonExpressionString vs = newInstance(orig, mode);
            if (vs == null) {
                assert false : this + "; " + mode;
                return this;
            }
            return vs;
        } finally {
            h.stop();
        }
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSingle(@NotNull Event e) {
        return toString(e);
    }

    @Override
    public String @NotNull [] getArray(@NotNull Event e) {
        return new String[]{toString(e)};
    }

    @Override
    public String @NotNull [] getAll(@NotNull Event e) {
        return new String[]{toString(e)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public boolean check(@NotNull Event e, @NotNull Checker<? super String> c, boolean negated) {
        return SimpleExpression.check(getAll(e), c, negated, false);
    }

    @Override
    public boolean check(@NotNull Event e, @NotNull Checker<? super String> c) {
        return SimpleExpression.check(getAll(e), c, false, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public <R> Expression<? extends R> getConvertedExpression(Class<R> @NotNull ... to) {
        if (CollectionUtils.containsSuperclass(to, String.class))
            return (Expression<? extends R>) this;
        return ConvertedExpression.newInstance(this, to);
    }

    @Override
    public @NotNull Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public Class<?> @NotNull [] acceptChange(@NotNull ChangeMode mode) {
        return CollectionUtils.array();
    }

    @Override
    public void change(@NotNull Event e, Object @NotNull [] delta, @NotNull ChangeMode mode) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getAnd() {
        return true;
    }

    @Override
    public boolean setTime(int time) {
        return false;
    }

    @Override
    public int getTime() {
        return 0;
    }

    @Override
    public boolean isDefault() {
        return false;
    }

    @Override
    public Iterator<? extends String> iterator(@NotNull Event e) {
        return new SingleItemIterator<>(toString(e));
    }

    @Override
    public boolean isLoopOf(@NotNull String s) {
        return false;
    }

    @Override
    public @NotNull Expression<?> getSource() {
        return this;
    }

    @Override
    public @NotNull Expression<String> simplify() {
        return this;
    }
}
