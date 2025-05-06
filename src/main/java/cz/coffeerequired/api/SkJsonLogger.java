package cz.coffeerequired.api;

import cz.coffeerequired.support.AnsiColorConverter;
import lombok.val;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Date;
import java.util.IllegalFormatException;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("ALL")
public abstract class SkJsonLogger {

    public static final String PREFIX = "&bSkJson&r";
    public final static Logger LOGGER = Bukkit.getLogger();


    private static final LegacyComponentSerializer converter =
            LegacyComponentSerializer
                    .builder()
                    .character('&')
                    .hexColors()
                    .build();

    @SuppressWarnings("deprecation")
    private static String legacy(String text) {
        return ChatColor.translateAlternateColorCodes('§', text);
    }

    private static boolean incorectFormatting(String s) {
        return s.contains("{") || s.contains("}");
    }

    public static void log(Level level, Object message, Object... args) {
        if (incorectFormatting(message.toString())) {
            message = message.toString()
                    .replaceAll("\\%\\{", "")
                    .replaceAll("\\}\\%", "");
        }

        var prefix = PREFIX;
        if (level == Level.INFO) {
            prefix = "&bSkJson&r";
        } else if (level == Level.WARNING) {
            prefix = "&bSkJson&e";
        }

        String msgText = message.toString();

        try {
            msgText = String.format(msgText, args);
        } catch (IllegalFormatException e) {
            msgText = msgText + " [FORMAT ERROR: " + e.getMessage() + "]";
        }

        var text = AnsiColorConverter.convertToAnsi("[" + prefix + "] " + msgText);
        LOGGER.log(level, text);
    }


    public static void ex(Throwable throwable, Object message, Object... args) {
        StringBuilder sb = new StringBuilder();

        sb.append("\u001B[31m")  // začátek červené
                .append("\n====== Exception Log Start ======\n")
                .append("Level: SEVERE\n")
                .append("Time: ").append(new Date()).append("\n")
                .append("Message: ").append(String.format((String) message, args)).append("\n")
                .append("=================================\n");

        if (throwable != null) {
            sb.append("Exception: ").append(throwable.getClass().getName()).append("\n")
                    .append("Description: ").append(throwable.getMessage()).append("\n")
                    .append("Stacktrace:\n");

            for (StackTraceElement element : throwable.getStackTrace()) {
                sb.append("    at ").append(element.toString()).append("\n");
            }
        }
        sb.append("======= Exception Log End =======\n\n")
                .append("\u001B[0m"); // reset barvy

        LOGGER.log(Level.SEVERE, sb.toString());
    }

    private static void entityMessage(Level level, CommandSender sender, Object message, Object[] args) {
        var formatted = String.format(message.toString(), args);
        TextComponent text;
        if (level == Level.SEVERE) {
            text = converter.deserialize("&l" + PREFIX + "&r&c " + formatted);
        } else if (level == Level.WARNING) {
            text = converter.deserialize("&l" + PREFIX + "&r&e " + formatted);
        } else if (level == Level.INFO) {
            text = converter.deserialize("&l" + PREFIX + "&r&7 " + formatted);
        } else {
            text = converter.deserialize("&l" + PREFIX + "&r&r " + formatted);
        }
        sender.sendMessage(text);
    }

    public static void info(CommandSender sender, Object message, Object[] args) {
        entityMessage(Level.INFO, sender, message, args);
    }

    public static void warning(CommandSender sender, Object message, Object... args) {
        entityMessage(Level.WARNING, sender, message, args);
    }

    public static void error(CommandSender sender, Object message, Object... args) {
        entityMessage(Level.SEVERE, sender, message, args);
    }

    public static String translate(String defaultStringifyJson) {

        val legacy = LegacyComponentSerializer.legacy('&');
        val deserialized = legacy.deserialize(defaultStringifyJson);
        return deserialized.content();
    }
}