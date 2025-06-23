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

    /**
     * Вызывается при включении плагина.
     */
    @Override
    public void onEnable() {
        saveDefaultConfig(); // Создаёт config.yml, если его нет
        config = getConfig(); // Загружает конфиг
        papiEnabled = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null; // Проверяет PlaceholderAPI

        getLogger().info("eHelper by AllFiRE успешно запущен!");
    }

    /**
     * Обработка команд.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("ehelper")) return false;

        if (args.length == 0) {
            sender.sendMessage(colorize(config.getString("messages.usage")));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                return true;
            case "send":
                handleSend(sender, args);
                return true;
            default:
                sender.sendMessage(colorize(config.getString("messages.usage")));
                return true;
        }
    }

    /**
     * Перезагружает конфиг.
     */
    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission(config.getString("permissions.reload"))) {
            sendNoPermission(sender, config.getString("permissions.reload"));
            return;
        }
        reloadConfig();
        config = getConfig();
        sender.sendMessage(colorize(config.getString("messages.reloaded")));
    }

    /**
     * Отправляет сообщение игроку.
     */
    private void handleSend(CommandSender sender, String[] args) {
        if (!sender.hasPermission(config.getString("permissions.use"))) {
            sendNoPermission(sender, config.getString("permissions.use"));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(colorize(config.getString("messages.usage")));
            return;
        }

        String targetName = (args.length >= 3) ? args[2] : sender.getName(); // Ник цели или отправителя
        sendDynamicMessage(sender, args[1], targetName);
    }

    /**
     * Формирует и отправляет кастомное сообщение.
     * @param sender Кто отправил команду.
     * @param messageId ID сообщения (1-4).
     * @param targetName Ник получателя.
     */
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
            message = PlaceholderAPI.setPlaceholders(target, message); // Подставляет плейсхолдеры PlaceholderAPI
        }

        target.sendMessage(colorize(message)); // Отправляет сообщение цели
        sender.sendMessage(colorize(config.getString("messages.sent-confirmation").replace("{player}", target.getName())));
    }

    /**
     * Уведомляет об отсутствии прав.
     */
    private void sendNoPermission(CommandSender sender, String permission) {
        String message = config.getString("messages.no-permission").replace("{permission}", permission);
        sender.sendMessage(colorize(message));
    }

    /**
     * Заменяет & на § для цветов в тексте.
     */
    private String colorize(String text) {
        return text.replace('&', '§');
    }
}
