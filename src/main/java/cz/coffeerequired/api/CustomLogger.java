package cz.coffeerequired.api;

import cz.coffeerequired.support.AnsiColorConverter;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;

import java.util.logging.*;

@SuppressWarnings("unused")
public class CustomLogger extends Logger {

    @Getter
    private static final String GRADIENT_PREFIX = "\u001B[38;5;74mS\u001B[38;5;75mk\u001B[38;5;81mJ\u001B[38;5;87ms\u001B[38;5;123mo\u001B[38;5;159mn\u001B[0m";
    @Getter
    static LegacyComponentSerializer converter = LegacyComponentSerializer.builder().useUnusualXRepeatedCharacterHexFormat().character('&').hexColors().build();

    public CustomLogger(String name) {
        super(name, null);
        setLevel(Level.ALL);
        initialize();
    }

    public Component colorize(String text) {
        return converter.deserialize(text);
    }

    private void initialize() {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new CustomFormatter());
        this.addHandler(handler);
        this.setUseParentHandlers(false);
    }

    @Override
    public void log(Level level, String message) {

        if (message.contains("gson") || (message.contains("Trying to find NMS support"))
                || message.contains("bStats")) return;

        if (message.contains("[NBTAPI]")) {
            return;
        }

        if (level == Level.INFO) {
            Bukkit.getConsoleSender().sendMessage(converter.deserialize("[" + GRADIENT_PREFIX + "]: " + message + "\u001B[0m"));
        } else if (level == Level.SEVERE) {
            super.log(level, message);
        } else {
            super.log(level, message);
        }
    }

    public void error(String message) {
        Bukkit.getConsoleSender().sendMessage("[" + GRADIENT_PREFIX + "]: " + AnsiColorConverter.RED + message + "\u001B[0m");
    }

    public void debug(String message) {
        Bukkit.getConsoleSender().sendMessage("[" + GRADIENT_PREFIX + "] " + "\u001B[38;5;245m:" + message + "\u001B[0m");
    }

    public void exception(String message, Throwable throwable) {
        log(Level.SEVERE, message, throwable);
    }

    private static class CustomFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            StringBuilder output = new StringBuilder();

            if (record.getLevel() == Level.INFO) {
                return String.format("[%s] [INFO]: %s\n", GRADIENT_PREFIX, record.getMessage());
            } else if (record.getLevel() == Level.SEVERE) {
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
                output.append(String.format("[%s] [%s]: %s\n", GRADIENT_PREFIX, record.getLevel().getName(), record.getMessage()));
            }
            return "";
        }
    }
}