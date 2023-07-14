package cz.coffee.skjson.api;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import static cz.coffee.skjson.api.Config.RUN_TEST_ON_START;

/**
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: středa (12.07.2023)
 */
public class SkJsonCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("skjson")) {
            if (args.length == 0) {
                sender.sendMessage(ColorWrapper.translate("&7Usage: &askjson reload"));
                sender.sendMessage(ColorWrapper.translate("&7Usage: &askjson run-tests"));
                return true;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                sender.sendMessage(ColorWrapper.translate(Config.PLUGIN_PREFIX + "⚠️ &econfig reloading..."));
                try {
                    Config.getConfig().loadConfigFile();
                    sender.sendMessage(ColorWrapper.translate(Config.PLUGIN_PREFIX + "&7New path delimiter: &e" + Config.PATH_VARIABLE_DELIMITER));
                    sender.sendMessage(ColorWrapper.translate(Config.PLUGIN_PREFIX + "&7reload &asuccessfully."));
                    return true;
                } catch (Exception ex) {
                    sender.sendMessage(ColorWrapper.translate(Config.PLUGIN_PREFIX + "&7reload &cunsuccessfully."));
                    return false;
                }
            } else if (args[0].equalsIgnoreCase("run-tests")) {
                if (Config.TESTS_ALOWED) {
                    CompletableFuture.runAsync(() -> {
                        try {
                            var loader = new SkriptLoaderFile(new File(Config.getConfig().plugin.getDataFolder() + "/" + "..tests"));
                            loader.load();
                            Thread.sleep(200);
                            loader.unload();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                }
                return true;
            }
            return false;
        }
        return false;
    }
}
