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

import static cz.coffeerequired.SkJson.logger;

public class Commands {
    private static final Map<String, CommandHandler> commandMap = new HashMap<>();

    @Setter
    private static String mainCommand = "";


    public static void add(String cmd,
                           BiConsumer<CommandSender, String[]> executor,
                           BiFunction<CommandSender, String[], List<String>> completer
    ) {
        if (cmd.contains("|")) {
             Arrays.stream(cmd.split("\\|"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(c -> commandMap.put(c, new CommandHandler(executor, completer)));
        } else {
            commandMap.put(cmd, new CommandHandler(executor, completer));
        }
    }


    public static void registerCommand(final JavaPlugin plugin) {
        var cmd = plugin.getCommand(mainCommand);
        var e = new NullPointerException("Command is null");
        if (cmd == null) logger().exception(e.getMessage(), e);
        else cmd.setExecutor(new CommandManager());
    }
    public static BiFunction<CommandSender, String[], List<String>> emptyCompleter() {
        return (a, b) -> List.of();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static BiConsumer<CommandSender, String[]> emptyCommand() {
        return (c, s) -> List.of();
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

            if (!sender.hasPermission(Api.Records.PROJECT_PERMISSION)) {
                sender.sendMessage(logger().colorize("&cYou don't have permission to use this command!"));
                return true;
            }

            if (args.length == 0) {
                sender.sendMessage(logger().colorize(
                        CustomLogger.getGRADIENT_PREFIX() +
                        "\nUsage: /" + label + " <command>" +
                        "\n &e - about|?" +
                        "\n &e - reload" +
                        "\n &e - status" +
                        "\n &e - debug"
                ));
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
