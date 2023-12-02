package cz.coffee.skjson.utils;

import ch.njol.skript.config.Node;
import ch.njol.skript.util.Version;
import cz.coffee.skjson.api.ColorWrapper;
import cz.coffee.skjson.api.Config;
import org.bukkit.Bukkit;
import org.bukkit.command.MessageCommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static cz.coffee.skjson.api.Config.*;

public abstract class LoggingUtil {

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

    /**
     * Enchanted error.
     *
     * @param ex            the ex
     * @param traceElements the trace elements
     * @param errorID       the error id
     */
    public static void enchantedError(Throwable ex, StackTraceElement[] traceElements, String errorID) {
        LoggingUtil.error("&4--------------------------- &l&cSkJson error handling &4---------------------------");
        LoggingUtil.error("          " + ex.getLocalizedMessage() + "          ");
        LoggingUtil.error("          " + errorID + "          ");
        LoggingUtil.error("&4------------------------------------------------------------------------------------");
        LoggingUtil.errorWithoutPrefix("");
        int i = traceElements.length;
        for (StackTraceElement st : traceElements) {
            int lineNumber = st.getLineNumber();
            String clazz = st.getFileName();
            String mess = st.getMethodName();
            LoggingUtil.errorWithoutPrefix(String.format("&e%s. &#eb6565%s &7(&f%s:%s&7)", i, mess, clazz, lineNumber));
            i--;
        }
        LoggingUtil.error("&4--------------------------- &l&cEnd of error handling &4---------------------------");
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

    /**
     * Log.
     *
     * @param msg the msg
     */
    public static void log(Object... msg) {
        Bukkit.getConsoleSender().sendMessage(ColorWrapper.translate(PLUGIN_PREFIX + Arrays.toString(msg)
                .replaceAll("^\\[", "")
                .replaceAll("]$", "")
        ));
    }

    /**
     * Watcher log.
     *
     * @param msg the msg
     */
    public static void watcherLog(String msg) {
        Bukkit.getConsoleSender().sendMessage(ColorWrapper.translate(PLUGIN_PREFIX + Config.WATCHER_PREFIX + msg));
    }

    /**
     * Webhook log.
     *
     * @param msg the msg
     */
    public static void webhookLog(String msg) {
        Bukkit.getConsoleSender().sendMessage(ColorWrapper.translate(PLUGIN_PREFIX + Config.WEBHOOK_PREFIX + msg));
    }

    /**
     * Error.
     *
     * @param msg the msg
     */
    public static void error(String msg) {
        Bukkit.getConsoleSender().sendMessage(ColorWrapper.translate(PLUGIN_PREFIX + Config.ERROR_PREFIX + "&l&c" + msg));
    }

    public static void warn(String msg, MessageCommandSender... msger) {
        if ((msger != null && msger.length > 0) && msger[0] != null) {
            msger[0].sendMessage(ColorWrapper.translate(PLUGIN_PREFIX + "&e&lWARN " + "&l&e" + msg));
        } else {
            Bukkit.getConsoleSender().sendMessage(ColorWrapper.translate(PLUGIN_PREFIX + "&e&lWARN " + "&l&e" + msg));
        }
    }

    @SuppressWarnings("unused")
    public static void error(boolean skript, String e) {
        Bukkit.getConsoleSender().sendMessage(ColorWrapper.translate(PLUGIN_PREFIX + Config.ERROR_PREFIX + "&l&c" + e));
    }

    /**
     * Error without prefix.
     *
     * @param msg the msg
     */
    public static void errorWithoutPrefix(String msg) {
        Bukkit.getConsoleSender().sendMessage(ColorWrapper.translate(PLUGIN_PREFIX + "&l&c" + msg));
    }

    /**
     * Error.
     *
     * @param msg  the msg
     * @param node the node
     */
    public static void error(String msg, @Nullable Node node) {
        int line = node == null ? 0 : node.getLine();
        assert node != null;
        Bukkit.getConsoleSender().sendMessage(ColorWrapper.translate(PLUGIN_PREFIX + "&c&lLine " + line + ":&8 (" + node.getConfig().getFileName() + ")"));
        Bukkit.getConsoleSender().sendMessage(ColorWrapper.translate("&#f27813\t" + msg));
    }

    /**
     * Request log.
     *
     * @param msg the msg
     */
    public static void requestLog(Object msg) {
        Bukkit.getConsoleSender().sendMessage(ColorWrapper.translate(REQUESTS_PREFIX + "&#fc4103Warning: &l&c " + msg));
    }
}




