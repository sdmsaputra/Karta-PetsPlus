package com.karta.petsplus;

import com.karta.petsplus.command.PetShopCommand;
import com.karta.petsplus.command.PetsCommand;
import com.karta.petsplus.command.PetsReloadCommand;
import com.karta.petsplus.listener.EntityListener;
import com.karta.petsplus.listener.GUIListener;
import com.karta.petsplus.listener.PlayerListener;
import com.karta.petsplus.manager.ConfigManager;
import com.karta.petsplus.manager.EconomyManager;
import com.karta.petsplus.manager.GuiManager;
import com.karta.petsplus.manager.MessageManager;
import com.karta.petsplus.manager.DatabaseManager;
import com.karta.petsplus.manager.PetManager;
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
    private MessageManager messageManager;
    private GuiManager guiManager;
    private DatabaseManager databaseManager;
    private PetManager petManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Initializing KartaPetsPlus...");

        // Setup configuration files
        configManager = new ConfigManager(this);
        configManager.loadConfigs();

        // Setup gui manager
        guiManager = new GuiManager(this);

        // Setup database manager
        databaseManager = new DatabaseManager(this);

        // Setup message manager
        messageManager = new MessageManager(this);

        // Setup data storage
        playerDataManager = new PlayerDataManager(this);

        // Setup pet manager
        petManager = new PetManager(this);

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

        // Register PlaceholderAPI expansion
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new com.karta.petsplus.placeholder.PlaceholderExpansion(this).register();
            getLogger().info("PlaceholderAPI expansion has been registered.");
        }

        getLogger().info("KartaPetsPlus has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        // Shutdown pet manager (despawns pets, stops scheduler)
        if (petManager != null) {
            petManager.shutdown();
        }

        // Save data for all online players before shutting down
        if (playerDataManager != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerDataManager.savePlayerPets(player);
            }
        }

        if (databaseManager != null) {
            databaseManager.close();
        }

        getLogger().info("KartaPetsPlus has been disabled.");
    }

    /**
     * Registers all of the plugin's commands.
     */
    private void registerCommands() {
        getCommand("pets").setExecutor(new PetsCommand(this));
        getCommand("pets").setTabCompleter(new com.karta.petsplus.command.PetsCommandCompleter(this));
        getCommand("petshop").setExecutor(new PetShopCommand(this));
        getCommand("petshop").setTabCompleter(new com.karta.petsplus.command.PetShopCommandCompleter(this));
        getCommand("petsreload").setExecutor(new PetsReloadCommand(this));
        getCommand("petsreload").setTabCompleter(new com.karta.petsplus.command.PetsReloadCommandCompleter(this));
        getLogger().info("Commands have been registered.");
    }

    /**
     * Registers all of the plugin's event listeners.
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityListener(this), this);
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

    /**
     * Gets the message manager.
     *
     * @return The message manager instance.
     */
    public MessageManager getMessageManager() {
        return messageManager;
    }

    /**
     * Gets the gui manager.
     *
     * @return The gui manager instance.
     */
    public GuiManager getGuiManager() {
        return guiManager;
    }

    /**
     * Gets the database manager.
     *
     * @return The database manager instance.
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /**
     * Gets the pet manager.
     *
     * @return The pet manager instance.
     */
    public PetManager getPetManager() {
        return petManager;
    }
}
