package com.karta.petsplus;

import com.karta.petsplus.commands.CommandManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PetsPlus extends JavaPlugin {

    private ConfigManager configManager;
    private MessageManager messageManager;
    private StorageManager storageManager;
    private PetManager petManager;
    private CommandManager commandManager;
    private ShopManager shopManager;
    private EconomyManager economyManager;
    private PurchaseHandler purchaseHandler;


    @Override
    public void onEnable() {
        // Initialize Managers
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        storageManager = new StorageManager(this);
        petManager = new PetManager(this);
        commandManager = new CommandManager(this);
        shopManager = new ShopManager(this);
        economyManager = new EconomyManager(this);
        purchaseHandler = new PurchaseHandler(this);


        // Initialize Storage
        storageManager.init();
        petManager.init();

        // Register Commands
        commandManager.registerCommands();

        // Register Listeners
        getServer().getPluginManager().registerEvents(new com.karta.petsplus.listeners.InventoryListener(), this);

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

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public PurchaseHandler getPurchaseHandler() {
        return purchaseHandler;
    }
}
