package cz.coffeerequired.api;

import cz.coffeerequired.support.AnsiColorConverter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import ch.njol.skript.config.Node;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.LogEntry;

import java.util.Date;
import java.util.IllegalFormatException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

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

    private static boolean incorectFormatting(String s) {
        return s.contains("{") || s.contains("}");
    }

    public static void log(Node node, Level level, Object message, Object... args) {
        String msgText = message.toString();
        if (incorectFormatting(msgText)) {
            msgText = msgText.replaceAll("\\%\\{", "").replaceAll("\\}\\%", "");
        }

        msgText = args != null && args.length > 0 ? String.format(msgText, args) : msgText;


        if (level.equals(Level.SEVERE)) {
            if (node != null) {
                Bukkit.getConsoleSender().sendMessage(translateComponent("[skJson] &4&lLine %s: &r&7(%s)".formatted(node.getLine(), node.getConfig().getFile().getName())));
                Bukkit.getConsoleSender().sendMessage(translateComponent("\t%s&4".formatted(msgText)));
                Bukkit.getConsoleSender().sendMessage(translateComponent("\t&6Line: &7%s".formatted(node.getPath())));
            }
        
        }
    }


    public static void log(Level level, Object message, Object... args) {
        if (message == null) {
            LOGGER.log(Level.WARNING, AnsiColorConverter.convertToAnsi("[" + PREFIX + "&e] Null message provided"));
            return;
        }

        String msgText = message.toString();
        if (incorectFormatting(msgText)) {
            msgText = msgText.replaceAll("\\%\\{", "").replaceAll("\\}\\%", "");
        }

        var prefix = PREFIX;
        String colorCode = "&r";
        if (level == Level.SEVERE) {
            prefix = "&bSkJson&c";
            colorCode = "&c";
        } else if (level == Level.WARNING) {
            prefix = "&bSkJson&e";
            colorCode = "&e";
        } else if (level == Level.INFO) {
            prefix = "&bSkJson&r";
        }

        try {
            msgText = args != null && args.length > 0 ? String.format(msgText, args) : msgText;
        } catch (IllegalFormatException ignored) {}

        var text = AnsiColorConverter.convertToAnsi("[" + prefix + "] " + colorCode + msgText);
        LOGGER.log(level, text);
    }

    public static void setNode(@Nullable Node node) {
        ParserInstance.get().setNode(node);
    }

    @Nullable
    public static Node getNode() {
        return ParserInstance.get().getNode();
    }

    public static void log(Level level, String message) {
        log(new LogEntry(level, message, getNode()));
    }

    public static void log(@Nullable LogEntry entry) {
        if (entry == null)
            return;
        LOGGER.log(entry.level, entry.message);
    }

    public static void ex(Throwable throwable, Object message, Object... args) {
        StringBuilder sb = new StringBuilder();

        sb.append("\u001B[31m")
                .append("\n====== Exception Log Start ======\n")
                .append("Level: SEVERE\n")
                .append("Time: ").append(new Date()).append("\n");

        try {
            String formattedMessage = message != null ? 
                (args != null && args.length > 0 ? String.format(message.toString(), args) : message.toString()) : 
                "No message provided";
            sb.append("Message: ").append(formattedMessage).append("\n");
        } catch (IllegalFormatException e) {
            sb.append("Message: [FORMAT ERROR] ").append(message).append("\n");
        }

        sb.append("=================================\n");

        if (throwable != null) {
            sb.append("Exception: ").append(throwable.getClass().getName()).append("\n")
                    .append("Description: ").append(throwable.getMessage() != null ? throwable.getMessage() : "No description").append("\n")
                    .append("Stacktrace:\n");

            for (StackTraceElement element : throwable.getStackTrace()) {
                sb.append("    at ").append(element.toString()).append("\n");
            }

            // Handle cause chain
            Throwable cause = throwable.getCause();
            while (cause != null) {
                sb.append("Caused by: ").append(cause.getClass().getName())
                        .append(": ").append(cause.getMessage() != null ? cause.getMessage() : "No description").append("\n");
                for (StackTraceElement element : cause.getStackTrace()) {
                    sb.append("    at ").append(element.toString()).append("\n");
                }
                cause = cause.getCause();
            }
        } else {
            sb.append("No exception details available\n");
        }

        sb.append("======= Exception Log End =======\n\n")
                .append("\u001B[0m");

        LOGGER.log(Level.SEVERE, sb.toString());
    }

    private static void entityMessage(Level level, CommandSender sender, Object message, Object[] args) {
        if (sender == null || message == null) {
            LOGGER.log(Level.WARNING, AnsiColorConverter.convertToAnsi("[" + PREFIX + "&e] Invalid sender or message"));
            return;
        }

        String formatted;
        try {
            formatted = args != null && args.length > 0 ? 
                String.format(message.toString(), args) : 
                message.toString();
        } catch (IllegalFormatException e) {
            LOGGER.log(Level.WARNING, AnsiColorConverter.convertToAnsi("[" + PREFIX + "&e] Format error in entityMessage: " + e.getMessage()));
            formatted = message.toString() + " [FORMAT ERROR]";
        }

        TextComponent text;
        try {
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
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, AnsiColorConverter.convertToAnsi("[" + PREFIX + "&c] Error sending message: " + e.getMessage()));
            ex(e, "Error in entityMessage");
        }
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
        return converter.deserialize(defaultStringifyJson).content();
    }

    public static Component translateComponent(String defaultStringifyJson) {
        return converter.deserialize(defaultStringifyJson);
    }
}