package cz.coffeerequired.modules;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import org.skriptlang.skript.common.function.DefaultFunction;
import com.google.gson.JsonElement;
import cz.coffeerequired.api.Extensible;
import cz.coffeerequired.api.Register;
import cz.coffeerequired.api.annotators.Module;
import cz.coffeerequired.api.requests.Attachment;
import cz.coffeerequired.api.requests.Request;
import cz.coffeerequired.api.requests.RequestMethod;
import cz.coffeerequired.api.requests.Response;
import cz.coffeerequired.skript.http.bukkit.HttpReceivedResponse;
import cz.coffeerequired.skript.http.effects.EffSendRequest;
import cz.coffeerequired.skript.http.events.ResponseReceive;
import cz.coffeerequired.skript.http.expressions.ExprGetPlayerIP;
import cz.coffeerequired.skript.http.expressions.ExprSimpleRequest;
import cz.coffeerequired.skript.http.expressions.requests.propExprAttachment;
import cz.coffeerequired.skript.http.expressions.requests.propExprQueryParams;
import cz.coffeerequired.skript.http.expressions.requests.propExprResponse;
import cz.coffeerequired.skript.http.expressions.responses.propExprResponseStatus;
import cz.coffeerequired.skript.http.expressions.responses.propExprResponseStatusCode;
import cz.coffeerequired.skript.http.expressions.simple.ExprSimpleBody;
import cz.coffeerequired.skript.http.expressions.simple.ExprSimpleHeaders;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

@Module(module = "http")
public class HttpModule extends Extensible {
    public HttpModule() {
        this.sign = this.getClass().getSimpleName();
        this.skriptElementPath = "cz.coffeerequired.skript.http";
    }

    @Override
    public void registerElements(Register.SkriptRegister register) {
        register.apply(this);


        register.registerExpression(ExprGetPlayerIP.class, String.class, ExpressionType.SIMPLE,
                "json-get %player% ip",
                "[the] ip [address] of %player%",
                "%player%'s ip [address]"
        );
        register.registerExpression(ExprSimpleRequest.class, Request.class, ExpressionType.SIMPLE,
                "prepare %requestmethod% request on %string%",
                "prepare [a] %requestmethod% request [to [url]] %string%",
                "create [a] %requestmethod% request [for [url]] %string%"
        );
        register.registerEffect(EffSendRequest.class,
                "execute %request% [as [(:non|:not)(-| )blocking]]",
                "send %request% [as [(:non|:not)(-| )blocking]]",
                "run %request% [as [(:non|:not)(-| )blocking]]"
        );

        Classes.registerClass(new EnumClassInfo<>(RequestMethod.class, "requestmethod", "request method")
                .user("request ?method?")
                .name("Request methods")
                .description("represent allowed methods for make a request, e.g. POST, GET")
                .examples("")
                .since("2.9")
        );

        Classes.registerClass(
                new ClassInfo<>(Request.class, "request")
                        .user("request?s")
                        .name("request")
                        .description("Representation instance of Request")
                        .since("2.9.9-pre API changes")
                        .parser(new Parser<>() {
                            @Override
                            public @NotNull String toString(Request request, int i) {
                                return request.toString();
                            }

                            @Override
                            public @NotNull String toVariableNameString(Request request) {
                                return request.toString();
                            }

                            @Override
                            public boolean canParse(@NonNull ParseContext context) {
                                return false;
                            }
                        })
        );


        Classes.registerClass(
                new ClassInfo<>(Response.class, "response")
                        .user("response?s")
                        .name("response")
                        .description("Representation instance of Response")
                        .since("5.0")
                        .parser(new Parser<>() {
                            @Override
                            public @NotNull String toString(Response response, int i) {
                                return response.toString();
                            }

                            @Override
                            public @NotNull String toVariableNameString(Response response) {
                                return response.toString();
                            }

                            @Override
                            public boolean canParse(@NonNull ParseContext context) {
                                return false;
                            }
                        })
        );

        register.registerFunction(DefaultFunction.builder(Register.getAddon(), "attachment", Attachment[].class)
                .description("Create new Attachment for the web request from path to file, when the file starts with */ the file will be found automatically.")
                .since("2.9.9 API Changes")
                .examples("attachment(\"*/test.json\") and attachment(\"*/config.sk\")")
                .parameter("object", String.class)
                .build(args -> new Attachment[]{new Attachment(args.get("object"))}));


        // ################ EVENTS ############################
        register.registerEvent(
                "*Http response received", ResponseReceive.class, HttpReceivedResponse.class,
                "Runs when an HTTP response is received.",
                "on http response",
                "5.0, 5.5",
                "received [http] response",
                "http response received"
        );

        register.registerEventValue(HttpReceivedResponse.class, Response.class, HttpReceivedResponse::getResponse,
                "event-response", "http response", "received response");

        register.registerProperty(propExprResponse.class, Response.class, "[last] response", "requests");


        register.registerSimplePropertyExpression(ExprSimpleBody.class, Object.class, "body", "requests/responses");
        register.registerSimplePropertyExpression(ExprSimpleHeaders.class, JsonElement.class, "header[s]", "requests/responses");

        register.registerProperty(propExprAttachment.class, Object.class, "attachments", "requests");
        register.registerProperty(propExprQueryParams.class, JsonElement.class, "query param(s|meters)", "requests");


        register.registerProperty(propExprResponseStatusCode.class, Integer.class, "status code", "response");
        register.registerProperty(propExprResponseStatus.class, String.class, "status", "response");

    }
}
