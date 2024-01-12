package cz.coffee.skjson.utils;

import ch.njol.skript.config.Node;
import ch.njol.skript.util.Version;
import cz.coffee.skjson.api.ColorWrapper;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

import static cz.coffee.skjson.api.ConfigRecords.*;
import static cz.coffee.skjson.utils.Util.fstring;

public abstract class Logger {

    static Component cstring(final String str, Object... arguments) {
        return ColorWrapper.translate(fstring(str, arguments));
    }

    static String times(final String str, int times) {
        String[] output = new String[times];
        Arrays.fill(output, str);
        return String.join("", output);
    }

    public static void error(Throwable throwable, CommandSender sender, Node node, Object... arguments) {
        if (sender == null) sender = Bukkit.getConsoleSender();
        sender.sendMessage("");
        String msg_ = fstring("&4%s &c&lSkJson error handling &4%s", times("-", 27), times("-", 27));
        sender.sendMessage(cstring(msg_));
        sender.sendMessage(cstring("  &c&lReason -> &e%s", throwable.getLocalizedMessage()));
        if (node != null) sender.sendMessage(cstring("  &c&lSkript-Node -> &e%s", node));
        if (arguments != null && arguments.length > 0 && arguments[0] != null)
            sender.sendMessage(cstring("  &cMessage -> &e%s", fstring("", true, arguments)));
        sender.sendMessage(cstring(times("&4-", msg_.length() - 8)));

        sender.sendMessage("");
        int i = throwable.getStackTrace().length;
        for (var st : throwable.getStackTrace()) {
            int line = st.getLineNumber();
            String cls = st.getFileName();
            String msg = st.getMethodName();
            sender.sendMessage(cstring("  &e%s. &#eb6565%s &7(&f%s:%s&7)", i, msg, cls, line));
            i--;
        }
        sender.sendMessage(cstring("&4%s &c&lEnd of error handling &4%s\n", times("-", 27), times("-", 27)));
    }

    public static void error(Throwable throwable, CommandSender sender, Object... arguments) {
        error(throwable, sender, null, arguments);
    }

    public static void error(Throwable throwable, Object... arguments) {
        error(throwable, null, arguments);
    }

    public static void simpleError(Object msg, Object... arguments) {
        simpleError(msg, null, arguments);
    }

    public static void simpleError(Object msg, CommandSender sender, Object... arguments) {
        if (sender == null) sender = Bukkit.getConsoleSender();
        if (msg instanceof Object[] objects) {
            var st = Arrays.stream(objects).map(Object::toString).toArray(String[]::new);
            sender.sendMessage(cstring(PLUGIN_PREFIX + String.join(" ", st), arguments));
        } else {
            sender.sendMessage(cstring(PLUGIN_PREFIX + msg.toString(), arguments));
        }
    }

    public static boolean versionError(Version userVersion, Version neededVersion, boolean disablePlugin, PluginManager manager, JavaPlugin plugin) {
        if (userVersion.isSmallerThan(neededVersion)) {
            Bukkit.getConsoleSender().sendMessage(ColorWrapper.translateLegacy(PLUGIN_PREFIX + ERROR_PREFIX + "&c-----------------------------------------------------------------------------------------------------------"));
            Bukkit.getConsoleSender().sendMessage(ColorWrapper.translateLegacy(PLUGIN_PREFIX + ERROR_PREFIX + "&cThis version doesn't support a older version of srkipt " + userVersion));
            Bukkit.getConsoleSender().sendMessage(ColorWrapper.translateLegacy(PLUGIN_PREFIX + ERROR_PREFIX + "&eUse older version &fhttps://github.com/SkJsonTeam/skJson/releases/tag/2.8.6"));
            Bukkit.getConsoleSender().sendMessage(ColorWrapper.translateLegacy(PLUGIN_PREFIX + ERROR_PREFIX + "Or update skript to &f2.7+"));
            Bukkit.getConsoleSender().sendMessage(ColorWrapper.translateLegacy(PLUGIN_PREFIX + ERROR_PREFIX + "&c-----------------------------------------------------------------------------------------------------------"));
            if (disablePlugin) manager.disablePlugin(plugin);
            return false;
        }
        return true;
    }

    public static void warn(Object msg, CommandSender sender, Object... arguments) {
        if (sender == null) sender = Bukkit.getConsoleSender();
        if (msg instanceof Object[] objects) {
            var st = Arrays.stream(objects).map(Object::toString).toArray(String[]::new);
            sender.sendMessage(cstring(PLUGIN_PREFIX + "&e&lWARN &e" + String.join(" ", st), arguments));
        } else {
            sender.sendMessage(cstring(PLUGIN_PREFIX + "&e&lWARN &e" + msg.toString(), arguments));
        }
    }

    public static void warn(Object msg, Object... arguments) {
        warn(msg, null, arguments);
    }

    public static void info(Object msg, CommandSender sender, Object... arguments) {
        if (sender == null) sender = Bukkit.getConsoleSender();
        if (msg instanceof Object[] objects) {
            var st = Arrays.stream(objects).map(Object::toString).toArray(String[]::new);
            sender.sendMessage(cstring(PLUGIN_PREFIX + "&r" + String.join(" ", st), arguments));
        } else {
            sender.sendMessage(cstring(PLUGIN_PREFIX + "&r" + msg.toString(), arguments));
        }
    }

    public static void info(Object msg, Object... arguments) {
        info(msg, null, arguments);
    }

    public static void webhookLog(String msg) {
        Bukkit.getConsoleSender().sendMessage(cstring("%s%s%s", PLUGIN_PREFIX, WEBHOOK_PREFIX, msg));
    }

    public static void watcherLog(String msg) {
        Bukkit.getConsoleSender().sendMessage(cstring("%s%s%s", PLUGIN_PREFIX, WATCHER_PREFIX, msg));
    }

    public static void requestLog(Object msg) {
        Bukkit.getConsoleSender().sendMessage(cstring("%s%s &#fc4103Warning: &l&c%s", REQUESTS_PREFIX, WATCHER_PREFIX, msg));
    }

    public static String coloredElement(String input) {
        return switch (input) {
            case "Expressions" -> "&aExpressions";
            case "Effects" -> "&bEffects";
            case "Events" -> "&5Events";
            case "Sections" -> "&fSections";
            case "Conditions" -> "&4Conditions";
            case "Functions" -> "&7Functions";
            case "Structures" -> "&9Structures";
            default -> input;
        };
    }


}
