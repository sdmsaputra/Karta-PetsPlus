package com.karta.petsplus;

import com.karta.petsplus.commands.CommandManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PetsPlus extends JavaPlugin {

    private ConfigManager configManager;
    private MessageManager messageManager;
    private StorageManager storageManager;
    private PetManager petManager;
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        // Initialize Managers
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        storageManager = new StorageManager(this);
        petManager = new PetManager(this);
        commandManager = new CommandManager(this);

        // Initialize Storage
        storageManager.init();

        // Register Commands
        commandManager.registerCommands();

        getLogger().info("KartaPetsPlus has been enabled!");
    }

    @Override
    public void onDisable() {
        // Shutdown Storage
        if (storageManager != null) {
            storageManager.shutdown();
        }
        getLogger().info("KartaPetsPlus has been disabled!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public PetManager getPetManager() {
        return petManager;
    }
}
