package com.karta.petsplus;

import com.karta.petsplus.listener.PlayerListener;
import com.karta.petsplus.manager.ConfigManager;
import com.karta.petsplus.command.PetsCommand;
import com.karta.petsplus.data.Pet;
import com.karta.petsplus.listener.PlayerListener;
import com.karta.petsplus.manager.ConfigManager;
import com.karta.petsplus.manager.DataManager;
import com.karta.petsplus.manager.EconomyManager;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class for the KartaPetsPlus plugin.
 * This class handles the initialization and shutdown of the plugin.
 */
public final class KartaPetsPlus extends JavaPlugin {

    private static KartaPetsPlus instance;
    private ConfigManager configManager;
    private EconomyManager economyManager;
    private DataManager dataManager;

    @Override
    public void onEnable() {
        instance = this;

        // Register serializable classes
        ConfigurationSerialization.registerClass(Pet.class);

        // Setup configuration
        configManager = new ConfigManager(this);

        // Setup economy
        economyManager = new EconomyManager(this);
        if (!economyManager.setup()) {
            getLogger().severe("Disabling KartaPetsPlus due to no economy provider being found.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Setup data manager
        dataManager = new DataManager(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(dataManager), this);

        // Register commands
        PetsCommand petsCommand = new PetsCommand(this);
        getCommand("pets").setExecutor(petsCommand);
        getCommand("pets").setTabCompleter(petsCommand);
        getCommand("petshop").setExecutor(new com.karta.petsplus.command.PetShopCommand(this));

        getLogger().info("KartaPetsPlus has been enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("KartaPetsPlus has been disabled!");
    }

    /**
     * Gets the singleton instance of the plugin.
     *
     * @return The KartaPetsPlus instance.
     */
    public static KartaPetsPlus getInstance() {
        return instance;
    }

    /**
     * Gets the configuration manager.
     *
     * @return The ConfigManager instance.
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Gets the economy manager.
     *
     * @return The EconomyManager instance.
     */
    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    /**
     * Gets the data manager.
     *
     * @return The DataManager instance.
     */
    public DataManager getDataManager() {
        return dataManager;
    }
}
