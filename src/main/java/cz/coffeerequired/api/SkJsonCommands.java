package cz.coffeerequired.api;

import cz.coffeerequired.support.Configuration;
import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;

import static cz.coffeerequired.SkJson.*;
import static cz.coffeerequired.api.Api.Records.*;

public abstract class SkJsonCommands {
    public static BiConsumer<CommandSender, String[]> configuration()
    {
        return (sender, args) -> {

            var builder = new StringBuilder();
            builder.append("\n=== configuration ===").append("\n");

            Api.Records.mapping.forEach((propertyName, configKey) -> {
                try {
                    var filed = Api.Records.class.getDeclaredField(propertyName);
                    if (!filed.canAccess(null)) filed.setAccessible(true);
                    builder.append("  ").append("&f* &7").append(configKey).append("&e: ").append(filed.get(null)).append("\n");
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });

            info(sender, builder.toString());

        };
    }

    @SuppressWarnings("UnstableApiUsage")
    public static BiConsumer<CommandSender, String[]> aboutAddon(PluginMeta plMeta, YamlConfiguration pluginConfig) {
        return (sender, s) -> {

            var builder = new StringBuilder();
            builder.append("\n=== about ===").append("\n");

            @SuppressWarnings("unchecked") ArrayList<String> list = (ArrayList<String>) pluginConfig.get("soft-depend");

            if (!sender.hasPermission("skjson.use")) {
                error(sender, "&cYou don't have permission to use this command.");
            } else {
                builder.append("&aVersion: &f").append(plMeta.getVersion()).append("\n");
                builder.append("&aWebsite: &9").append(plMeta.getWebsite()).append("\n");
                builder.append("&aRevision: &6").append(pluginConfig.get("revision-version")).append("\n");
                builder.append("&aDescription: &e").append(plMeta.getDescription()).append("\n");
                builder.append("&aDependencies: &3").append(plMeta.getPluginDependencies()).append("\n");
                builder.append("&6Soft-dependencies: &7").append(list).append("\n");
            }
            info(sender, builder.toString());
        };
    }

    public static BiConsumer<CommandSender, String[]> reloadAddon(Configuration configuration) {
        return (sender, s) -> {
            info(sender, "🟠 &econfig reloading...");
            try {
                final WeakHashMap<String, ?> before = new WeakHashMap<>(Map.ofEntries(
                        Map.entry("PROJECT_DEBUG", PROJECT_DEBUG),
                        Map.entry("CONFIG_VERSION", CONFIG_VERSION),
                        Map.entry("PROJECT_PERMISSION", PROJECT_PERMISSION),

                        Map.entry("PROJECT_DELIM", PROJECT_DELIM),

                        Map.entry("PROJECT_ENABLED_HTTP", PROJECT_ENABLED_HTTP),
                        Map.entry("PROJECT_ENABLED_NBT", PROJECT_ENABLED_NBT),
                        Map.entry("HTTP_MAX_THREADS", HTTP_MAX_THREADS),


                        Map.entry("WATCHER_INTERVAL", WATCHER_INTERVAL),
                        Map.entry("WATCHER_REFRESH_RATE", WATCHER_REFRESH_RATE),
                        Map.entry("WATCHER_WATCH_TYPE", WATCHER_WATCH_TYPE),
                        Map.entry("WATCHER_MAX_THREADS", WATCHER_MAX_THREADS),
                        Map.entry("DISABLED_UPDATE", DISABLED_UPDATE),
                        Map.entry("PLUGIN_FALLBACK_ENABLED", PLUGIN_FALLBACK_ENABLED)
                ));
                configuration.getHandler().reloadConfig();
                Boolean[] changed = new Boolean[]{false};

                before.forEach((key, value) -> {
                    try {
                        var field = Api.Records.class.getDeclaredField(key);
                        field.setAccessible(true);
                        var fieldValue = field.get(null);

                        if (!value.equals(fieldValue)) {
                            if (!changed[0]) changed[0] = true;
                            info(sender, "&7The field %s has been changed from &e%s &7 to &f%s", Configuration.getMapping(key), value, fieldValue);
                        }
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        exception(e, "Cannot handle that field %s", key);
                    }
                });
            } catch (Exception ex) {
                exception(ex, "An error occurred while reloading configuration");
            }
        };
    }
}
