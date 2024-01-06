package cz.coffee.skjson.api;

import cz.coffee.skjson.SkJson;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static cz.coffee.skjson.api.Config.*;

/**
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: středa (12.07.2023)
 */
@SuppressWarnings("ALL")
public class SkJsonCommand implements CommandExecutor {
    String formatDesc(String desc) {
        if (desc.contains("%nl%")) {
            var builder = new StringBuilder();
            var parts = desc.split("%nl%");
            for (String part : parts) {
                builder.append("\n").append("\t\t  ").append("- ").append(part);
            }
            return builder.toString();
        }
        return desc;
    }

    private void sendAbout(CommandSender sender) {
        sender.sendMessage(ColorWrapper.translate("&7SkJson revision version: &a" + Config.pluginYaml.get("revision-version")));
        sender.sendMessage(ColorWrapper
                .translate("&7Description: &f" +
                        formatDesc(Config.getConfig().plugin.getPluginMeta().getDescription()))
        );
        sender.sendMessage(ColorWrapper
                .translate("&7SkJson version: &f" + Config.getConfig().plugin.getPluginMeta().getVersion()));
        sender.sendMessage(ColorWrapper.translate("&7Author: &a" + Config.getConfig().plugin.getPluginMeta().getAuthors()));
        sender.sendMessage(ColorWrapper.translate("&7API-version: &6" + Config.getConfig().plugin.getPluginMeta().getAPIVersion()));
        sender.sendMessage(ColorWrapper.translate("&7Website: &f" + Config.getConfig().plugin.getPluginMeta().getWebsite()));
        sender.sendMessage(ColorWrapper.translate("&7GitHub: &f" + "https://www.github.com/SkJsonTeam/SkJson"));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("skjson")) {
            if (args.length == 0) {
                sender.sendMessage(ColorWrapper.translate("&7Usage: &a/skjson reload"));
                sender.sendMessage(ColorWrapper.translate("&7Usage: &a/skjson about"));
                return true;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                sender.sendMessage(ColorWrapper.translate(Config.PLUGIN_PREFIX + "🟠 &econfig reloading..."));
                try {
                    final HashMap<String, ?> before = new HashMap<>(Map.ofEntries(
                            Map.entry("CONFIG_VERSION", CONFIG_VERSION),
                            Map.entry("PROJECT_DEBUG", PROJECT_DEBUG),
                            Map.entry("LOGGING_LEVEL", LOGGING_LEVEL),
                            Map.entry("DEFAULT_WATCHER_INTERVAL", DEFAULT_WATCHER_INTERVAL),
                            Map.entry("PLUGIN_PREFIX", PLUGIN_PREFIX),
                            Map.entry("ERROR_PREFIX", ERROR_PREFIX),
                            Map.entry("WATCHER_PREFIX", WATCHER_PREFIX),
                            Map.entry("REQUESTS_PREFIX", REQUESTS_PREFIX),
                            Map.entry("WEBHOOK_PREFIX", WEBHOOK_PREFIX),
                            Map.entry("PATH_VARIABLE_DELIMITER", PATH_VARIABLE_DELIMITER),
                            Map.entry("ALLOWED_LINE_LITERAL", ALLOWED_LINE_LITERAL)
                    ));
                    Config.getConfig().loadConfigFile(false);
                    AtomicBoolean changed = new AtomicBoolean(false);
                    before.forEach((key, value) -> {
                        try {
                            Field field = Config.class.getDeclaredField(key);
                            field.setAccessible(true);
                            Object fieldValue = field.get(null);
                            // Porovnejte hodnotu ve fieldu s hodnotou v mapě
                            if (!value.equals(fieldValue)) {
                                if (changed.get() == false) changed.set(true);

                                sender.sendMessage(
                                        ColorWrapper.translate(PLUGIN_PREFIX + String.format(
                                                "&7The field &e'%s'&7 was changed from&8 '%s'&7 to &a'%s'", getMapping(key), value, fieldValue
                                        ))
                                );

                                if (key.equals("CONFIG_VERSION") && SkJson.ConfigVERSION != CONFIG_VERSION) {
                                    var brokenFile = Config.getConfig().loadConfigFile(true, sender, true);
                                    sender.sendMessage(ColorWrapper.translate(
                                            "🔴 &cThe config version was changed! Config will be regenerate...\n\t\t   &cWrong config was saved to " + brokenFile
                                    ));
                                }
                            }
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    });
                    if (changed.get() == false) {
                        sender.sendMessage(
                                ColorWrapper.translate(PLUGIN_PREFIX + "Nothing was changed.")
                        );
                    }
                    sender.sendMessage(ColorWrapper.translate(Config.PLUGIN_PREFIX + "&7reload &asuccessfully."));
                    return true;
                } catch (Exception ex) {
                    sender.sendMessage(ColorWrapper.translate(Config.PLUGIN_PREFIX + "&7reload &cunsuccessfully."));
                    return false;
                }
            } else if (args[0].equalsIgnoreCase("about")) {
                sendAbout(sender);
                return true;
            }
            return false;
        }
        sender.sendMessage(ColorWrapper.translate("&7Usage: &a/skjson reload"));
        sender.sendMessage(ColorWrapper.translate("&7Usage: &a/skjson about"));
        return true;
    }
}
