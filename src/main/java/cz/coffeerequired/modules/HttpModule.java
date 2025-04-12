package cz.coffeerequired.modules;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.lang.function.SimpleJavaFunction;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.DefaultClasses;
import ch.njol.skript.registrations.EventValues;
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
import cz.coffeerequired.skript.http.eventexpressions.ExprEvtResponse;
import cz.coffeerequired.skript.http.events.ResponseReceive;
import cz.coffeerequired.skript.http.expressions.*;
import cz.coffeerequired.skript.http.expressions.requests.*;
import cz.coffeerequired.skript.http.expressions.responses.propExprResponseBody;
import cz.coffeerequired.skript.http.expressions.responses.propExprResponseHeaders;
import cz.coffeerequired.skript.http.expressions.responses.propExprResponseStatus;
import cz.coffeerequired.skript.http.expressions.responses.propExprResponseStatusCode;
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


        register.registerExpression(ExprGetPlayerIP.class, String.class, ExpressionType.SIMPLE, "json-get %player% ip");
        register.registerExpression(ExprSimpleRequest.class, Request.class, ExpressionType.SIMPLE, "prepare %requestmethod% request on %string%");
        register.registerEffect(EffSendRequest.class, "execute %request% [as [(:non|:not)(-| )blocking]]");

        Classes.registerClass(new EnumClassInfo<>(RequestMethod.class, "requestmethod", "request method")
                .user("request ?method?")
                .name("Request methods")
                .description("represent allowed methods for make a request, e.g. POST, GET")
                .examples("")
                .since("2.9")
        );

        Classes.registerClass(
                new ClassInfo<>(Request.class, "request")
                        .user("request")
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

        Parameter<?>[] string = new Parameter[]{new Parameter<>("object", DefaultClasses.STRING, true, null)};

        register.registerFunction(new SimpleJavaFunction<>("attachment", string, DefaultClasses.OBJECT, true) {
                    @Override
                    @SuppressWarnings("all")
                    public Attachment @NotNull [] executeSimple(Object[][] params) {
                        String data = params[0][0].toString();
                        return new Attachment[]{new Attachment(data)};
                    }
                })
                .description("Create new Attachment for the web request from path to file, when the file starts with */ the file will be found automatically.")
                .since("2.9.9 API Changes")
                .examples("attachment(\"*/test.json\") and attachment(\"*/config.sk\")");


        // ################ EVENTS ############################
        register.registerEvent(
                "*Http response received", ResponseReceive.class, HttpReceivedResponse.class,
                "will return last http response",
                "on response received",
                "5.0",
                "received [http] response"
        );

        EventValues.registerEventValue(HttpReceivedResponse.class, Response.class, HttpReceivedResponse::getResponse, EventValues.TIME_NOW);
        register.registerEventValueExpression(ExprEvtResponse.class, Response.class, "event-response");

        register.registerProperty(propExprAttachment.class, Object.class, "[request] attachments", "requests");
        register.registerProperty(propExprContent.class, JsonElement.class, "[request] body", "requests");
        register.registerProperty(propExprHeader.class, JsonElement.class, "[request] header[s]", "requests");
        register.registerProperty(propExprQueryParams.class, JsonElement.class, "[request] query param(s|meters)", "requests");
        register.registerProperty(propExprResponse.class, Response.class, "[last] response", "requests");

        register.registerProperty(propExprResponseBody.class, Object.class, "(body|content)", "responses");
        register.registerProperty(propExprResponseStatusCode.class, Integer.class, "status code", "responses");
        register.registerProperty(propExprResponseStatus.class, String.class, "status", "responses");
        register.registerProperty(propExprResponseHeaders.class, JsonElement.class, "header[s]", "responses");

    }
}
