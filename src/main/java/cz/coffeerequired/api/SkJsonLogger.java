package cz.coffeerequired.api;

import cz.coffeerequired.support.AnsiColorConverter;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("ALL")
public abstract class SkJsonLogger {

    public static final String PREFIX = "SkJson";
    public final static Logger LOGGER = Bukkit.getLogger();

    private static final LegacyComponentSerializer converter =
            LegacyComponentSerializer
                    .builder()
                    .character('&')
                    .hexColors()
                    .build();

    @SuppressWarnings("deprecation")
    private static String legacy(String text) { return ChatColor.translateAlternateColorCodes('§', text); }

    public static void log(Level level, Object message, Object... args) {
        var formatted = AnsiColorConverter.convertToAnsi("[" + PREFIX + "] " + message.toString());
        var text = String.format(formatted, args);
        LOGGER.log(level, text);
    }


    public static void ex(Throwable e, Object message, Object... args) {
        e.printStackTrace();
    }

    private static void entityMessage(Level level, CommandSender sender, Object message, Object[] args) {
        var formatted = String.format(message.toString(), args);
        TextComponent text;
        if (level == Level.SEVERE) {
            text = converter.deserialize("[" +PREFIX+ "]&c " + formatted);
        } else if (level == Level.WARNING) {
            text = converter.deserialize("[" +PREFIX+ "]&e " + formatted);
        } else if (level == Level.INFO) {
            text = converter.deserialize("[" +PREFIX+ "]&7 " + formatted);
        } else {
            text = converter.deserialize("[" +PREFIX+ "]&r " + formatted);
        }
        sender.sendMessage(text);
    }

    public static void info(CommandSender sender, Object message, Object[] args) {
        entityMessage(Level.INFO, sender, message, args);
    }

    public static void warning(CommandSender sender, Object message, Object ... args) {
        entityMessage(Level.WARNING, sender, message, args);
    }

    public static void error(CommandSender sender, Object message, Object ... args) {
        entityMessage(Level.SEVERE, sender, message, args);
    }

    public static String translate(String defaultStringifyJson) {
        return ChatColor.translateAlternateColorCodes('&', defaultStringifyJson);
    }

/*    private static class CustomFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            StringBuilder output = new StringBuilder();
            if (record.getLevel() == Level.SEVERE) {
                output.append("\u001B[31m");
                output.append("\n====== Exception Log Start ======\n");
                output.append("Level: SEVERE\n");
                output.append("Time: ").append(new java.util.Date(record.getMillis())).append("\n");
                output.append("Source: ").append(record.getSourceClassName()).append(".").append(record.getSourceMethodName()).append("\n");
                output.append("Message: ").append(record.getMessage()).append("\n");
                output.append("=================================\n");

                if (record.getThrown() != null) {
                    Throwable throwable = record.getThrown();
                    output.append("Exception: ").append(throwable.getClass().getName()).append("\n");
                    output.append("Description: ").append(throwable.getMessage()).append("\n");
                    output.append("Stacktrace:\n");
                    for (StackTraceElement element : throwable.getStackTrace()) {
                        output.append("    at ").append(element.toString()).append("\n");
                    }
                }
                output.append("======= Exception Log End =======\n\n");
                output.append("\u001B[0m");
                Bukkit.getConsoleSender().sendMessage(output.toString());
            } else {
                output.append(String.format("[%s] [%s]: %s\n", PREFIX, record.getLevel().getName(), record.getMessage()));
            }
            return "";
        }
    }*/


}