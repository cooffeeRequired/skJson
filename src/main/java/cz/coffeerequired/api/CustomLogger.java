package cz.coffeerequired.api;

import cz.coffeerequired.support.AnsiColorConverter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class CustomLogger extends Logger {

    private static final String GRADIENT_PREFIX = "\u001B[38;5;74mS\u001B[38;5;75mk\u001B[38;5;81mJ\u001B[38;5;87ms\u001B[38;5;123mo\u001B[38;5;159mn\u001B[0m";
    static LegacyComponentSerializer converter = LegacyComponentSerializer.builder().useUnusualXRepeatedCharacterHexFormat().character('&').hexColors().build();

    public CustomLogger(String name) {
        super(name, null);
        setLevel(Level.ALL);
        initialize();
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
        if (level == Level.INFO) {
            Bukkit.getConsoleSender().sendMessage("[" + GRADIENT_PREFIX + "]: " + message + "\u001B[0m");
        } else if (level == Level.SEVERE) {
            log(Level.SEVERE, message);
        } else {
            super.log(level, message);
        }
    }

    public static Component colorize(String text) {
        return converter.deserialize(text);
    }

    public void error(String message) {
        Bukkit.getConsoleSender().sendMessage("[" + GRADIENT_PREFIX + "]: " + AnsiColorConverter.RED  + message + "\u001B[0m");
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
            }
            else if (record.getLevel() == Level.SEVERE) {
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
