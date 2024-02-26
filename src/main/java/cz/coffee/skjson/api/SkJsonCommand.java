package cz.coffee.skjson.api;

import cz.coffee.skjson.SkJson;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static cz.coffee.skjson.api.Config.getMapping;
import static cz.coffee.skjson.api.ConfigRecords.CONFIG_VERSION;
import static cz.coffee.skjson.api.ConfigRecords.PLUGIN_PREFIX;
import static cz.coffee.skjson.utils.Logger.info;

/**
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: středa (12.07.2023)
 */
@SuppressWarnings("ALL")
public class SkJsonCommand implements TabExecutor {
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
                info("&7Usage: &a/skjson reload");
                info("&7Usage: &a/skjson about");
                return true;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                info("%s 🟠 &econfig reloading...", PLUGIN_PREFIX);
                try {
                    final HashMap<String, ?> before = new HashMap<>(Map.ofEntries(
                            Map.entry("CONFIG_VERSION", ConfigRecords.CONFIG_VERSION),
                            Map.entry("PROJECT_DEBUG", ConfigRecords.PROJECT_DEBUG),
                            Map.entry("LOGGING_LEVEL", ConfigRecords.LOGGING_LEVEL),
                            Map.entry("DEFAULT_WATCHER_INTERVAL", ConfigRecords.DEFAULT_WATCHER_INTERVAL),
                            Map.entry("PLUGIN_PREFIX", ConfigRecords.PLUGIN_PREFIX),
                            Map.entry("ERROR_PREFIX", ConfigRecords.ERROR_PREFIX),
                            Map.entry("WATCHER_PREFIX", ConfigRecords.WATCHER_PREFIX),
                            Map.entry("REQUESTS_PREFIX", ConfigRecords.REQUESTS_PREFIX),
                            Map.entry("WEBHOOK_PREFIX", ConfigRecords.WEBHOOK_PREFIX),
                            Map.entry("PATH_VARIABLE_DELIMITER", ConfigRecords.PATH_VARIABLE_DELIMITER),
                            Map.entry("ALLOWED_LINE_LITERAL", ConfigRecords.ALLOWED_LINE_LITERAL)
                    ));
                    Config.getConfig().loadConfigFile(false);
                    AtomicBoolean changed = new AtomicBoolean(false);
                    before.forEach((key, value) -> {
                        try {
                            Field field = ConfigRecords.class.getDeclaredField(key);
                            field.setAccessible(true);
                            Object fieldValue = field.get(null);
                            // Porovnejte hodnotu ve fieldu s hodnotou v mapě
                            if (!value.equals(fieldValue)) {
                                if (changed.get() == false) changed.set(true);
                                info("%s &7The field &e'%s'&7 was changed from&8 '%s'&7 to &a'%s'", PLUGIN_PREFIX, getMapping(key), value, fieldValue);

                                if (key.equals("CONFIG_VERSION") && SkJson.CONFIG_PRIMARY_VERSION != CONFIG_VERSION) {
                                    var brokenFile = Config.getConfig().loadConfigFile(true, sender, true);
                                    info(
                                            "🔴 &cThe config version was changed! Config will be regenerate...\n\t\t   &cWrong config was saved to %s", brokenFile
                                    );
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
                    sender.sendMessage(ColorWrapper.translate(PLUGIN_PREFIX + "&7reload &asuccessfully."));
                    return true;
                } catch (Exception ex) {
                    sender.sendMessage(ColorWrapper.translate(PLUGIN_PREFIX + "&7reload &cunsuccessfully."));
                    return false;
                }
            } else if (args[0].equalsIgnoreCase("about") || args[0].equalsIgnoreCase("?")) {
                sendAbout(sender);
                return true;
            }
            return false;
        }
        sender.sendMessage(ColorWrapper.translate("&7Usage: &a/skjson reload"));
        sender.sendMessage(ColorWrapper.translate("&7Usage: &a/skjson about"));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        ArrayList<String> completations = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("skjson")) {
            switch (strings.length) {
                case 1 -> {
                    completations.add("reload");
                    completations.add("about");
                    completations.add("?");
                }
            }
        }
        return completations;
    }
}
