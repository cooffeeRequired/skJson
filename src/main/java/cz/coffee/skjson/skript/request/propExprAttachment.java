package cz.coffee.skjson.skript.request;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import cz.coffee.skjson.SkJsonElements;
import cz.coffee.skjson.api.requests.Attachment;
import cz.coffee.skjson.api.requests.Request;
import cz.coffee.skjson.api.requests.RequestMethod;
import cz.coffee.skjson.utils.Logger;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;

import static cz.coffee.skjson.utils.Util.fstring;

@Name("Request attachment/s")
@Examples("""
             # getting the Request attachment;
             send {_request}'s attachments
             send attachments of {_request}
            \s
             # setting the Request attachment;
             set {_request}'s attachments to attachment("*/test.sk") and attachment("*/raw.json")
             set attachments of {_request} to attachment("*/test.sk") and attachment("*/raw.json")
            \s
             # add the attachment to the Request attachments
             add attachment("*/SkJson.json") to {_request}'s attachments
             add attachment("*/SkJson.json") to attachments of {_request}
            \s
             # reset the attachments of the Request
             reset {_request}'s attachments
             reset attachments of {_request}
    \s""")
@Description("set/add/reset or get the current request attachment")
@Since("2.9.9-pre Api Changes")
@ApiStatus.Experimental
public class propExprAttachment extends PropertyExpression<Request, Object> {

    static {
        SkJsonElements.registerProperty(propExprAttachment.class, Object.class,
            "[request] attachments",
            "requests"
        );
    }

    @Override
    protected Object @NotNull [] get(@NotNull Event event, Request @NotNull [] source) {
        return Arrays.stream(source).filter(Objects::nonNull).map(Request::attachments).toArray(Object[]::new);
    }

    @Override
    public @NotNull Class<?> getReturnType() {
        return Object.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        assert event != null;
        return fstring("attachment/s of %s", getExpr().toString(event, debug));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        setExpr((Expression<? extends Request>) expressions[0]);
        return true;
    }

    @Override
    @SuppressWarnings("all")
    public Class<?>[] acceptChange(Changer.@NotNull ChangeMode mode) {
        return switch (mode) {
            case RESET -> CollectionUtils.array();
            case SET, ADD -> CollectionUtils.array(Attachment.class, Attachment[].class);
            default -> null;
        };
    }

    @Override
    public void change(@NotNull Event event, Object @NotNull [] delta, Changer.@NotNull ChangeMode mode) {
        var request = getExpr().getSingle(event);
        assert request != null;

        if (!request.method().equals(RequestMethod.POST)) {
            var e = new IllegalStateException(fstring("Cannot set attachments to %s method.. Allowed methods are [POST]", request.method()));
            Logger.error(e, null, getParser().getNode());
            return;
        }
        if (mode == Changer.ChangeMode.SET) {
            LinkedList<Attachment> attachments = new LinkedList<>();
            for (var d : delta) {
                if (d instanceof Attachment att) {
                    attachments.add(att);
                }
            }
            request.setAttachments(attachments);
        } else if (mode == Changer.ChangeMode.ADD) {
            LinkedList<Attachment> attachments = request.attachments();
            for (var d : delta) {
                if (d instanceof Attachment att) {
                    attachments.add(att);
                }
            }
        } else if (mode == Changer.ChangeMode.RESET) {
            request.setAttachments(new LinkedList<>());
        }
    }
}