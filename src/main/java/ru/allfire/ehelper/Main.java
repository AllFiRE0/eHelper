package ru.allfire.ehelper;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private FileConfiguration config;
    private boolean papiEnabled;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        papiEnabled = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        getLogger().info("eHelper by AllFiRE запущен!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("ehelper")) return false;

        if (args.length == 0) {
            sender.sendMessage(colorize(config.getString("messages.usage")));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission(config.getString("permissions.reload"))) {
                    sendNoPermission(sender, config.getString("permissions.reload"));
                    return true;
                }
                reloadConfig();
                config = getConfig();
                sender.sendMessage(colorize(config.getString("messages.reloaded")));
                return true;

            case "send":
                if (!(sender instanceof Player) && args.length < 3) {
                    sender.sendMessage("Укажите ник игрока: /ehelper send <1-4> <ник>");
                    return true;
                }
                String targetName = args.length >= 3 ? args[2] : sender.getName();
                sendDynamicMessage(sender, args[1], targetName);
                return true;

            default:
                sender.sendMessage(colorize(config.getString("messages.usage")));
                return true;
        }
    }

    private void sendDynamicMessage(CommandSender sender, String messageId, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(colorize(config.getString("messages.player-not-found").replace("{player}", targetName)));
            return;
        }

        String messageKey = "command-messages." + messageId;
        if (!config.contains(messageKey)) {
            sender.sendMessage(colorize("&cСообщение с ID " + messageId + " не найдено!"));
            return;
        }

        String message = config.getString(messageKey)
                .replace("{player}", target.getName())
                .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()));

        if (papiEnabled) {
            message = PlaceholderAPI.setPlaceholders(target, message);
        }

        target.sendMessage(colorize(message));
        sender.sendMessage(colorize(config.getString("messages.sent-confirmation").replace("{player}", target.getName())));
    }

    private void sendNoPermission(CommandSender sender, String permission) {
        sender.sendMessage(colorize(config.getString("messages.no-permission").replace("{permission}", permission)));
    }

    private String colorize(String text) {
        return text != null ? text.replace('&', '§') : "";
    }
}
