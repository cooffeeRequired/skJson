package cz.coffee.skjson.skript.request;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import cz.coffee.skjson.SkJson;
import cz.coffee.skjson.SkJsonElements;
import cz.coffee.skjson.api.http.RequestClient;
import cz.coffee.skjson.api.http.RequestResponse;
import cz.coffee.skjson.api.requests.Request;
import cz.coffee.skjson.api.requests.RequestStatus;
import cz.coffee.skjson.api.requests.Response;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.ws.rs.core.UriBuilder;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ch.njol.skript.util.LiteralUtils.canInitSafely;
import static ch.njol.skript.util.LiteralUtils.defendExpression;
import static cz.coffee.skjson.utils.Logger.error;

@Name("Send created/prepared request")
@Examples("send prepared {_request}")
@Description("Send prepared/created request to the given method and uri")
@Since("2.9.9-pre Api Changes")
@ApiStatus.Experimental
public class EffSendRequest extends Effect {

    private static final Field DELAYED;
    private static final ExecutorService threadPool =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    static {
        Field _DELAYED = null;
        try {
            _DELAYED = Delay.class.getDeclaredField("DELAYED");
            _DELAYED.setAccessible(true);
        } catch (NoSuchFieldException e) {
            error(new IllegalStateException("Skript's 'delayed' method could not be resolved. Some Skript warnings may not be available."));
        }
        DELAYED = _DELAYED;
    }

    static {
        SkJsonElements.registerEffect(EffSendRequest.class,
                "[:sync] (send|execute) [prepared] %request%"
        );
    }

    Expression<Request> exprRequest;
    boolean sync;

    @Override
    protected void execute(@NotNull Event event) {
        var request = exprRequest.getSingle(event);
        assert request != null;
        if (sync) {
            var response = sendRequest(request);
            assert response != null;
            var rsp = new Response(response.getStatusCode(), response.getBodyContent(true), response.getResponseHeader().json());
            request.setResponse(rsp);
        } else {
            var vars = Variables.copyLocalVariables(event);
            CompletableFuture.supplyAsync(() -> sendRequest(request), threadPool)
                    .whenComplete((resp, err) -> {
                        if (err != null) {
                            error(err, null, getParser().getNode());
                            request.setResponse(Response.empty());
                            return;
                        }
                        if (resp != null) {
                            Bukkit.getScheduler().runTask(SkJson.getInstance(), () -> {
                                var rsp = new Response(resp.getStatusCode(), resp.getBodyContent(true), resp.getResponseHeader().json());
                                request.setResponse(rsp);
                                Variables.setLocalVariables(event, vars);
                                if (getNext() != null) TriggerItem.walk(getNext(), event);
                            });
                        }
                    });
        }
    }

    @Override
    protected TriggerItem walk(@NotNull Event e) {
        var rq = this.exprRequest.getSingle(e);
        if (rq == null) return null;
        debug(e, true);
        if (!sync) delay(e);
        execute(e);
        if (sync) return super.walk(e);
        return null;
    }

    private RequestResponse sendRequest(Request request) {
        boolean hasAttachments = !request.attachments().isEmpty();

        URI URL = null;
        try {
            var uri = UriBuilder.fromUri(request.uri());
            if (!request.getQueryParams().isEmpty())
                request.getQueryParams().forEach(uri::queryParam);
            URL = uri.build();
        } catch (Exception ex) {
            error(ex, null, getParser().getNode());
        }

        try (var client = new RequestClient(URL != null ? URL.toString() : request.uri())) {
            RequestResponse rsp;
            if (hasAttachments) {
                client.setAttachments(request.attachments());
                rsp = client.method(request.method().toString())
                        .setHeaders(request.header())
                        .postAttachments(request.content())
                        .request(true)
                        .get();
            } else {
                rsp = client.method(request.method().toString())
                        .setHeaders(request.header())
                        .setContent(request.content())
                        .request(true)
                        .get();
            }
            if (rsp != null) {
                request.setStatus(rsp.isSuccessfully() ? RequestStatus.OK : RequestStatus.FAILED);
                return rsp;
            }
        } catch (ExecutionException | InterruptedException ex) {
            error(ex, null, getParser().getNode());
        }
        return null;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        assert event != null;
        return "execute prepared " + this.exprRequest.toString(event, debug);
    }

    @SuppressWarnings("unchecked")
    private void delay(Event e) {
        if (DELAYED != null) {
            try {
                ((Set<Event>) DELAYED.get(null)).add(e);
            } catch (IllegalAccessException illegalAccessException) {
                error(illegalAccessException, null, getParser().getNode());
            }
        }
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        exprRequest = defendExpression(expressions[0]);
        sync = parseResult.hasTag("sync");
        return canInitSafely(exprRequest);
    }
}
