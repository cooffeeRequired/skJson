package cz.coffeerequired.modules;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import com.google.gson.JsonElement;
import cz.coffeerequired.api.Extensible;
import cz.coffeerequired.api.Register;
import cz.coffeerequired.api.annotators.Module;
import cz.coffeerequired.api.requests.Request;
import cz.coffeerequired.api.requests.RequestMethod;
import cz.coffeerequired.skript.http.effects.EffSendRequest;
import cz.coffeerequired.skript.http.expressions.*;
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
        register.registerExpression(ExprSimpleRequest.class, Request.class, ExpressionType.SIMPLE, "prepare %requestmethod% request on %string%");
        register.registerEffect(EffSendRequest.class, "[:sync] (send|execute) %request%");
        register.registerProperty(propExprAttachment.class, Object.class, "[request] attachments", "requests");
        register.registerProperty(propExprContent.class, JsonElement.class, "[request] content", "requests");
        register.registerProperty(propExprHeader.class, JsonElement.class, "[request] header[s]", "requests");
        register.registerProperty(propExprQueryParams.class, JsonElement.class, "[request] query params(s|meters)", "requests");
        register.registerProperty(propExprResponse.class, Object.class, "response [:content|:headers|:status code|:status]", "requests");


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
    }
}
