package cz.coffeerequired.skript.http.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.AsyncEffect;
import ch.njol.util.Kleenean;
import cz.coffeerequired.api.http.RequestClient;
import cz.coffeerequired.api.requests.Request;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import static ch.njol.skript.util.LiteralUtils.canInitSafely;
import static ch.njol.skript.util.LiteralUtils.defendExpression;


@Name("Send created/prepared request")
@Examples({"""
                # blocking - synchronous
                set {_request} to prepare GET request on "https://dummyjson.com/products/1"
                set {_request}'s headers to "{'Content-Type':'application/json'}"
                execute {_request}
               \s
                set {_response} to last response of {_request}

                if {_response}'s status is "OK":
                    send "Request was successful!"
                    send {_response}'s body
                    send {_response}'s headers
                    send {_response}'s status code
                else:
                    send "Request failed!"
       \s""",
        """
            # non-blocking - asynchronous
            command /test:
                trigger:
                    # non-blocking - asynchronous
                    set {_request} to prepare GET request on "https://dummyjson.com/products/1"
                    set {_request}'s headers to "{'Content-Type':'application/json'}"
                    execute {_request} as non blocking

            on received http response:
                if event-response's status is "OK":
                    send "Request was successful!"
                    send event-response's body
                    send event-response's headers
                    send event-response's status code
                else:
                    send "Request failed!"
        """}
    )
@Description({
        "Sends a previously created HTTP request to the specified URL",
        "The request must be created using request creation commands (e.g. 'create request').",
        "Supports both synchronous and asynchronous sending using the 'non' or 'not' tag."
})
@Since({"2.9.9-pre Api Changes", "5.0"})
public class EffSendRequest extends AsyncEffect {

    private Expression<Request> requests;
    private boolean isAsync;

    @Override
    protected void execute(Event event) {
        Request request = requests.getSingle(event);
        var requestHandler = new RequestClient.Handler(request, event);

        if (isAsync) {
            requestHandler.runAsNonBlocking();
        } else {
            requestHandler.run();
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "execute request %s".formatted(requests.toString(event, debug));
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        requests = defendExpression(expressions[0]);
        isAsync = parseResult.hasTag("non") || parseResult.hasTag("not");
        return canInitSafely(requests);
    }
}