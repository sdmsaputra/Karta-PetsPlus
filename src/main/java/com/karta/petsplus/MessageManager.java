package com.karta.petsplus;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class MessageManager {

    private final PetsPlus plugin;
    private FileConfiguration messagesConfig;
    private File messagesFile;

    public MessageManager(PetsPlus plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    public void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void reloadMessages() {
        if (messagesFile == null) {
            messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getMessage(String path) {
        return getMessage(path, new String[]{});
    }

    public String getMessage(String path, String... placeholders) {
        String message = messagesConfig.getString(path, "&cMessage not found: " + path);
        String prefix = messagesConfig.getString("prefix", "");
        message = message.replace("%prefix%", prefix);

        if (placeholders.length > 0 && placeholders.length % 2 == 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                message = message.replace(placeholders[i], placeholders[i + 1]);
            }
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public java.util.List<String> getHelpMessage() {
        java.util.List<String> helpLines = messagesConfig.getStringList("help-message");
        helpLines.replaceAll(line -> ChatColor.translateAlternateColorCodes('&', line));
        return helpLines;
    }
}
