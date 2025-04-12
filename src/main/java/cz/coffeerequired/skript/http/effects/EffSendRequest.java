package cz.coffeerequired.skript.http.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.util.AsyncEffect;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.http.MimeMultipartData;
import cz.coffeerequired.api.http.RequestClient;
import cz.coffeerequired.api.http.RequestResponse;
import cz.coffeerequired.api.requests.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.*;

import static ch.njol.skript.util.LiteralUtils.canInitSafely;
import static ch.njol.skript.util.LiteralUtils.defendExpression;


@Name("Send created/prepared request")
@Examples("send prepared {_request}")
@Description("Send prepared/created request to the given method and uri")
@Since("2.9.9-pre Api Changes")
@ApiStatus.Experimental
public class EffSendRequest1 extends AsyncEffect {



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