package cz.coffeerequired.skript.core;

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
import cz.coffeerequired.api.json.GsonParser;
import cz.coffeerequired.api.json.SerializedJsonUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ch.njol.skript.util.LiteralUtils.canInitSafely;
import static ch.njol.skript.util.LiteralUtils.defendExpression;
import static cz.coffeerequired.api.Api.Records.PROJECT_DEBUG;

public abstract class SupportSkriptJson {



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
    public static class JsonLoopExpression extends SimpleExpression<Object> {



        private boolean isKey;
        private String name;
        private SecLoop loop;
        private boolean isCanceled = false;

        @SuppressWarnings("unchecked")
        @Override
        protected @Nullable Object[] get(Event event) {
            if (isCanceled) return new Object[0];

            HashMap<String, Object> outputMap;
            try {
                outputMap = (HashMap<String, Object>) loop.getCurrent(event);
            } catch (ClassCastException exception) {
                if (PROJECT_DEBUG) SkJson.logger().exception(exception.getLocalizedMessage(), exception);
                return new Object[0];
            }

            if (outputMap == null) return new Object[0];

            for (Map.Entry<String, Object> entry : outputMap.entrySet()) {
                if (isKey) return new String[]{entry.getKey()};
                Object[] first = (Object[]) Array.newInstance(getReturnType(), 1);
                if (entry.getValue() instanceof JsonElement element) {
                    Object assignedValue = GsonParser.fromJson(element);
                    if (assignedValue == null) assignedValue = SerializedJsonUtils.lazyJsonConverter(element);
                    first[0] = assignedValue;
                } else {
                    first[0] = entry.getValue();
                }
                return first;
            }
            return new Object[0];
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
            if (event == null) return name;
            return Classes.getDebugMessage(loop.getCurrent(event));
        }

        @Override
        public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull SkriptParser.ParseResult parseResult) {
            MatchResult numberOfLoop = !parseResult.regexes.isEmpty() ? parseResult.regexes.getFirst() : null;
            Object group = 0;
            if (numberOfLoop != null) group = numberOfLoop.group(0);
            int i = 0;
            isKey = parseResult.hasTag("key");
            String firstField = parseResult.expr, s = "";
            Pattern pattern = Pattern.compile("json-(.+)(.)");
            Matcher matchingPattern = pattern.matcher(firstField);

            if (matchingPattern.matches()) {
                String[] split = firstField.split("-");
                s = split[1];
                i = Objects.requireNonNull(SerializedJsonUtils.isNumeric(group)).intValue();
            }
            Class<?> inputClass = Classes.getClassFromUserInput(s);
            name = s;
            int j = 1;

            SecLoop loop = null;
            for (SecLoop l : getParser().getCurrentSections(SecLoop.class)) {
                if ((inputClass != null && inputClass.isAssignableFrom(l.getLoopedExpression().getReturnType())) || l.getLoopedExpression().isLoopOf("value")) {
                    if (j < i) {
                        j++;
                        continue;
                    }
                    if (loop != null) {
                        isCanceled = true;
                        break;
                    }
                    loop = l;
                    if (j == i) break;
                }
            }

            if (loop == null) {
                Skript.error("There's no loop that matches 'json-" + s + " "+group+"'", ErrorQuality.SEMANTIC_ERROR);
                return false;
            }
            if (isCanceled) {
                Skript.error("There are multiple loops that match json-" + s + ". Use json-" + s + "-1/2/3/etc. to specify witch loop's value you want.", ErrorQuality.SEMANTIC_ERROR);
                return false;
            }
            this.loop = loop;
            return true;
        }

        public boolean isLoopOf(@NotNull String s) {
            return false;
        }
    }


    @Name("Support literals")
    @Description("Represent sort of literals for skJson as like 1st/2nd, first, last, random etc.")
    @Since("4.1 - API UPDATE")
    @Examples("""
    set {_json} to json from "[1, 2, 3, 8, 'TEST']"

    send 1st value of {_json}
    send 2nd value of {_json}
    send last value of {_json}
    send random value of {_json}
    send 4. value of {_json}
    """)
    public static class JsonSupportElement extends SimpleExpression<Object> {

        private SearchType searchType;
        private Type type;
        private Expression<JsonElement> jsonVariable;
        private Expression<Integer> userCustomIndexInput;


        @Override
        protected @Nullable Object[] get(Event event) {
            final JsonElement json = jsonVariable.getSingle(event);
            if (json == null) return new Object[0];
            return new Object[] {switch (type) {
                case FIRST -> SerializedJsonUtils.getFirst(json, searchType);
                case LAST -> SerializedJsonUtils.getLast(json, searchType);
                case SECOND -> SerializedJsonUtils.get(json, 1, searchType);
                case THIRD -> SerializedJsonUtils.get(json, 2, searchType);
                case RANDOM -> SerializedJsonUtils.getRandom(json, searchType);
                case CUSTOM -> {
                    Integer index = userCustomIndexInput.getSingle(event);
                    if (index == null) yield null;
                    yield SerializedJsonUtils.get(json, index - 1, searchType);
                }
            }};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<?> getReturnType() {
            return Object.class;
        }

        @Override
        public String toString(@Nullable Event event, boolean debug) {
            return switch (type) {
                case FIRST -> "1st";
                case SECOND -> "2nd";
                case THIRD -> "3rd";
                case LAST -> "last";
                case RANDOM -> "random";
                case CUSTOM -> userCustomIndexInput.toString(event, debug) + ".";
            } + (searchType.equals(SearchType.KEY) ? " key" : " value") + " of " + jsonVariable.toString(event, debug);
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
            searchType = parseResult.hasTag("key") ? SearchType.KEY : SearchType.VALUE;
            type = switch (matchedPattern) {
                case 0 -> Type.FIRST;
                case 1 -> Type.SECOND;
                case 2 -> Type.THIRD;
                case 3 -> Type.LAST;
                case 4 -> Type.RANDOM;
                case 5 -> Type.CUSTOM;
                default -> null;
            };
            jsonVariable = defendExpression(expressions[0]);
            if (type.equals(Type.CUSTOM)) {
                userCustomIndexInput = (Expression<Integer>) expressions[0];
                jsonVariable = defendExpression(expressions[1]);
                if (userCustomIndexInput == null) return false;
            }
            return canInitSafely(jsonVariable);
        }

        public enum SearchType { VALUE, KEY }
        public enum Type { FIRST, SECOND, THIRD, LAST, RANDOM, CUSTOM }
    }
}
