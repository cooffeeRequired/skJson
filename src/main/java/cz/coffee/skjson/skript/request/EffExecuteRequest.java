package cz.coffee.skjson.skript.request;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import cz.coffee.skjson.SkJson;
import cz.coffee.skjson.api.SkriptReflection;
import cz.coffee.skjson.api.http.RequestResponse;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.WeakHashMap;

@NoDoc
public class EffExecuteRequest extends Effect {

    static {
        SkJson.registerEffect(EffExecuteRequest.class,
                "(execute|run) %objects% [and (:wait)]"
        );
    }

    private Expression<Request> requestExpression;
    private Kleenean runsAsync;
    private boolean waiting;

    @Override
    protected TriggerItem walk(@NotNull Event event) {
        if (runsAsync.isUnknown()) {
            return super.walk(event);
        }

        Request request = requestExpression.getSingle(event);
        if (request == null) return getNext();
        boolean needContinue = waiting && getNext() != null;

        Object localVars = needContinue ? SkriptReflection.removeLocals(event) : null;
        boolean ranAsync = !Bukkit.isPrimaryThread();

        Runnable runSection = () -> {
            SkriptReflection.putLocals(localVars, event);

            RequestEvent sectionEvent = request.execute();
            storeResult(sectionEvent, event);

            if (needContinue) {
                Runnable continuation = () -> {
                    TriggerItem.walk(getNext(), event);
                    SkriptReflection.removeLocals(event);
                };
                runTask(continuation, ranAsync);
            }
        };

        if (needContinue)
            Delay.addDelayedEvent(event);

        runTask(runSection, runsAsync.isTrue());

        return needContinue ? null : getNext();

    }

    @Override
    protected void execute(@NotNull Event event) {
        Request section = requestExpression.getSingle(event);
        if (section != null) {
            RequestEvent sectionEvent = section.execute();
            storeResult(sectionEvent, event);
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean b) {
        return "run " + Classes.getDebugMessage(requestExpression) + " [ " + Classes.getDebugMessage(this) + " ]";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, @NotNull ParseResult parseResult) {
        requestExpression = LiteralUtils.defendExpression(expressions[0]);
        runsAsync = Kleenean.TRUE;
        Expression<?> variableStorage = LiteralUtils.defendExpression(expressions[0]);

        if (!(variableStorage instanceof Variable<?>)) {
            throw RequestException.wrongStoreVar();
        }

        waiting = parseResult.hasTag("wait");
        if (!runsAsync.isUnknown() && !waiting)
            Skript.warning("You need to wait until the section is finished if you want to get a result.");

        if (!runsAsync.isUnknown() && waiting)
            getParser().setHasDelayBefore(Kleenean.TRUE);

        return LiteralUtils.canInitSafely(variableStorage);
    }

    private void storeResult(RequestEvent rEvent, Event event) {
        RequestResponse response = rEvent.getResponse();
        WeakHashMap<String, Variable<?>> vars = rEvent.getVarMap();
        boolean saveIncorrect = rEvent.isSaveIncorrect();

        if (response != null && !vars.isEmpty()) {
            if (vars.get("content") != null) vars.get("content").change(event, new Object[]{response.getBodyContent(saveIncorrect)}, Changer.ChangeMode.SET);
            if (vars.get("header") != null) vars.get("header").change(event, new Object[]{response.getResponseHeader().text()}, Changer.ChangeMode.SET);
            if (vars.get("status") != null) vars.get("status").change(event, new Object[]{response.getStatusCode()}, Changer.ChangeMode.SET);
            if (vars.get("url") != null) vars.get("url").change(event, new Object[]{response.getRequestURL()}, Changer.ChangeMode.SET);
        }
    }

    private void runTask(Runnable task, boolean async) {
        if (async)
            Bukkit.getScheduler().runTaskAsynchronously(SkJson.getInstance(), task);
        else
            Bukkit.getScheduler().runTask(SkJson.getInstance(), task);
    }
}
