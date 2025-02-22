package cz.coffeerequired.skript.http.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;

public class ExprGetPlayerIP extends SimpleExpression<String> {

    Expression<Player> playerExpression;

    @Override
    protected String @Nullable [] get(Event event) {
        Player p = playerExpression.getSingle(event);
        if (p == null) return new String[0];
        InetSocketAddress address = p.getAddress();
        if (address == null) return new String[0];
        return new String[]{address.getHostName()};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "get player ip";
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        playerExpression = (Expression<Player>) expressions[0];
        return true;
    }
}