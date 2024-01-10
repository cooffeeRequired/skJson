package cz.coffee.skjson.skript.request;

import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import cz.coffee.skjson.SkJson;
import cz.coffee.skjson.api.http.RequestClient;
import cz.coffee.skjson.api.http.RequestResponse;
import cz.coffee.skjson.utils.LoggingUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ch.njol.skript.util.LiteralUtils.canInitSafely;
import static ch.njol.skript.util.LiteralUtils.defendExpression;

@ApiStatus.Experimental
public abstract class Request {
    static RequestRecord CURRENT_REQUEST;
    public static class ExprMakeRequest extends SimpleExpression<RequestWrapper> {

        static {
            SkJson.registerExpression(ExprMakeRequest.class, RequestWrapper.class, ExpressionType.SIMPLE,
                    "(make|create) %requestmethod% request to %string%"
            );
        }

        private Expression<RequestMethods> _method;
        private Expression<String> _uri;

        @Override
        protected RequestWrapper @NotNull [] get(@NotNull Event event) {
            final RequestMethods method = _method.getSingle(event);
            final String uri = _uri.getSingle(event);
            assert method != null;
            assert uri != null;
            return new RequestWrapper[]{new RequestWrapper(method.toString(), uri)};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public @NotNull Class<? extends RequestWrapper> getReturnType() {
            return RequestWrapper.class;
        }

        @Override
        public @NotNull String toString(@Nullable Event event, boolean b) {
            return "";
        }

        @Override
        public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, @NotNull ParseResult parseResult) {
            _method = defendExpression(expressions[0]);
            _uri = defendExpression(expressions[1]);
            return canInitSafely(_method) && canInitSafely(_uri);
        }
    }
    public static class EffSendRequest extends Effect {

        static {
            SkJson.registerEffect(EffSendRequest.class, "execute %request%");
        }

        private static final Field DELAYED;
        private static final ExecutorService threadPool =
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        static {
            Field _DELAYED = null;
            try {
                _DELAYED = Delay.class.getDeclaredField("DELAYED");
                _DELAYED.setAccessible(true);
            } catch (NoSuchFieldException e) {
                LoggingUtil.enchantedError(e, e.getStackTrace(), e.getLocalizedMessage());
            }
            DELAYED = _DELAYED;
        }

        private Expression<RequestWrapper> _request;
        @Override
        protected void execute(@NotNull Event event) {
            var vars = Variables.copyLocalVariables(event);
            RequestWrapper request = _request.getSingle(event);
            assert request != null;

            CompletableFuture.supplyAsync(() -> {
                RequestResponse response = null;
                try (var client = new RequestClient(request.getUri())) {
                    response = client
                            .method(request.getMethod())
                            .setHeaders(request.getInputHeaders())
                            .setContent(request.getInputContent())
                            .request(request.isLenient())
                            .get();
                    if (response != null) {
                        return new RequestRecord(response.getStatusCode(), response.getResponseHeader(), new RequestUtil.JsonOrString(response.getBodyContent(request.isSaveIncorrect())));
                    }
                    return null;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, threadPool).whenComplete((resp, err) -> {
                if (err != null) {
                    LoggingUtil.enchantedError(err, err.getStackTrace(), err.getLocalizedMessage());
                    return;
                }
                if (resp == null) {
                    return;
                }
                Bukkit.getScheduler().runTask(SkJson.getInstance(), () -> {
                    Variables.setLocalVariables(event, vars);
                    CURRENT_REQUEST = resp;
                    if (getNext() != null) {
                        TriggerItem.walk(getNext(), event);
                    }
                });
            });
        }

        @Override
        public @NotNull String toString(@Nullable Event event, boolean b) {
            return "";
        }

        @Override
        protected TriggerItem walk(@NotNull Event e) {
            debug(e, true);
            wait(e);
            execute(e);
            return null;
        }

        @SuppressWarnings("unchecked")
        private void wait(Event event) {
            if (DELAYED != null) {
                try {
                    ((Set<Event>) DELAYED.get(null)).add(event);
                } catch (IllegalAccessException e) {
                    LoggingUtil.enchantedError(e, e.getStackTrace(), e.getLocalizedMessage());
                }
            }
        }

        @Override
        public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, @NotNull ParseResult parseResult) {
            _request = defendExpression(expressions[0]);
            return canInitSafely(_request);
        }
    }
    public static class ExprRequestValues extends SimpleExpression<Object> {
        @Override
        protected Object @NotNull [] get(@NotNull Event event) {
            return new Object[0];
        }

        @Override
        public boolean isSingle() {
            return false;
        }

        @Override
        public @NotNull Class<?> getReturnType() {
            return null;
        }

        @Override
        public @NotNull String toString(@Nullable Event event, boolean b) {
            return "";
        }

        @Override
        public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, @NotNull ParseResult parseResult) {
            return false;
        }
    }
}
