package cz.coffee.skjson.api;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

import java.util.logging.Logger;

public class SkJsonLogger extends Logger {
    protected SkJsonLogger(String name, String resourceBundleName) {
        super(name, resourceBundleName);
    }

    public static SkJsonLogger getLogger(String name) {
        return new SkJsonLogger(name, null);
    }

    @Override
    public void info(String message) {
        if (message.contains("gson") || (message.contains("Trying to find NMS support"))
                || message.contains("bStats")) return;

        if (message.contains("[NBTAPI]")) {
            message = message.replace("[NBTAPI]","&#adfa6eN&#53db88B&#00b797T&#009294A&#006c7eP&#2a4858I &r");
        }
        ConsoleCommandSender sender = Bukkit.getConsoleSender();
        sender.sendMessage(ColorWrapper.translate(Config.PLUGIN_PREFIX + message));
    }
}
