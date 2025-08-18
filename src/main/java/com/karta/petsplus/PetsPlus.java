package com.karta.petsplus;

import com.karta.petsplus.commands.CommandManager;
import com.karta.petsplus.listeners.InventoryListener;
import com.karta.petsplus.listeners.PetListener;
import com.karta.petsplus.shop.ShopManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PetsPlus extends JavaPlugin {

    private ConfigManager configManager;
    private MessageManager messageManager;
    private StorageManager storageManager;
    private PetManager petManager;
    private CommandManager commandManager;
    private ShopManager shopManager;


    @Override
    public void onEnable() {
        // Initialize Managers
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        storageManager = new StorageManager(this);
        petManager = new PetManager(this);
        commandManager = new CommandManager(this);
        // ShopManager needs to be initialized after others it depends on
        shopManager = new ShopManager(this, storageManager, messageManager);


        // Initialize Storage and Managers
        storageManager.init();
        petManager.init();
        shopManager.init();

        // Register Commands
        commandManager.registerCommands();

        // Register Listeners
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new PetListener(this), this);

        getLogger().info("KartaPetsPlus has been enabled!");
    }

    @Override
    public void onDisable() {
        // Shutdown PetManager first to save data
        if (petManager != null) {
            petManager.shutdown();
        }

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

    public ShopManager getShopManager() {
        return shopManager;
    }
}
