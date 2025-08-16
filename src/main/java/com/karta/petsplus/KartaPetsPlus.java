package com.karta.petsplus;

import com.karta.petsplus.command.PetShopCommand;
import com.karta.petsplus.command.PetsCommand;
import com.karta.petsplus.command.PetsReloadCommand;
import com.karta.petsplus.listener.PlayerListener;
import com.karta.petsplus.manager.ConfigManager;
import com.karta.petsplus.manager.EconomyManager;
import com.karta.petsplus.manager.PlayerDataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class for the KartaPetsPlus plugin.
 * This class handles the plugin's lifecycle, including startup and shutdown,
 * and manages integration with economy providers like Vault and PlayerPoints.
 */
public final class KartaPetsPlus extends JavaPlugin {

    private EconomyManager economyManager;
    private ConfigManager configManager;
    private PlayerDataManager playerDataManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Initializing KartaPetsPlus...");

        // Setup configuration files
        configManager = new ConfigManager(this);
        configManager.loadConfigs();

        // Setup data storage
        playerDataManager = new PlayerDataManager(this);

        // Initialize economy manager and check for dependencies
        economyManager = new EconomyManager(this);
        if (!economyManager.setupEconomy()) {
            getServer().getConsoleSender().sendMessage(Component.text("[KartaPetsPlus] No supported economy plugin found (Vault or PlayerPoints). Disabling plugin.", NamedTextColor.RED));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register commands and listeners
        registerCommands();
        registerListeners();

        getLogger().info("KartaPetsPlus has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        // Save data for all online players before shutting down
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerDataManager.savePlayerPets(player);
        }
        getLogger().info("KartaPetsPlus has been disabled.");
    }

    /**
     * Registers all of the plugin's commands.
     */
    private void registerCommands() {
        getCommand("pets").setExecutor(new PetsCommand(this));
        getCommand("petshop").setExecutor(new PetShopCommand(this));
        getCommand("petsreload").setExecutor(new PetsReloadCommand(this));
        getLogger().info("Commands have been registered.");
    }

    /**
     * Registers all of the plugin's event listeners.
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getLogger().info("Listeners have been registered.");
    }

    /**
     * Gets the currently active economy manager.
     *
     * @return The economy manager instance.
     */
    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    /**
     * Gets the configuration manager.
     *
     * @return The config manager instance.
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Gets the player data manager.
     *
     * @return The player data manager instance.
     */
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
}
