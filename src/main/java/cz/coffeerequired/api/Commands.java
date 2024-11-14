package cz.coffeerequired.api;

import lombok.Setter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class Commands {
    private static final Map<String, CommandHandler> commandMap = new HashMap<>();

    @Setter private static String mainCommand = "";


    public static void add(String cmd,
                           BiConsumer<CommandSender, String[]> executor,
                           BiFunction<CommandSender, String[], List<String>> completer
    ) {
        commandMap.put(cmd, new CommandHandler(executor, completer));
    }


    public static void registerCommand(final JavaPlugin plugin) {
        Objects.requireNonNull(plugin.getCommand(mainCommand)).setExecutor(new CommandManager());
    }

    private record CommandHandler(BiConsumer<CommandSender, String[]> commandExecutor,
                                  BiFunction<CommandSender, String[], List<String>> tabCompleter) {

        public List<String> complete(CommandSender sender, String... args) {
            return tabCompleter.apply(sender, args);
        }

        public void execute(CommandSender sender, String... args) {
            commandExecutor.accept(sender, args);
        }
    }

    private static class CommandManager implements TabExecutor {

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (args.length == 0) {
                sender.sendMessage("No arguments");
                return true;
            }

            CommandHandler handler = commandMap.get(args[0].toLowerCase());
            if (handler == null) sender.sendMessage("Unknown command");
            else handler.execute(sender, args);
            return true;
        }

        @Override
        public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (args.length == 1) return new ArrayList<>(commandMap.keySet());

            CommandHandler handler = commandMap.get(args[0].toLowerCase());
            if (handler == null) return new ArrayList<>();
            return handler.complete(sender, args);
        }
    }
}
