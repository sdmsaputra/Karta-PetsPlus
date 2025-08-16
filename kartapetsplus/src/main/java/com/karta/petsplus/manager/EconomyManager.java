package com.karta.petsplus.manager;

import com.karta.petsplus.KartaPetsPlus;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Manages economy integrations with Vault and PlayerPoints.
 * Handles checking for dependencies, selecting the economy provider,
 * and processing transactions.
 */
public class EconomyManager {

    private final KartaPetsPlus plugin;
    private Economy vaultEconomy = null;
    private PlayerPointsAPI playerPointsAPI = null;
    private CurrencyType activeCurrency;

    public enum CurrencyType {
        VAULT,
        PLAYER_POINTS,
        NONE
    }

    /**
     * Constructs a new EconomyManager.
     *
     * @param plugin The main plugin instance.
     */
    public EconomyManager(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    /**
     * Sets up the economy provider.
     * This method should be called during plugin startup.
     *
     * @return True if a valid economy provider was found, false otherwise.
     */
    public boolean setup() {
        // Check for Vault
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                vaultEconomy = rsp.getProvider();
            }
        }

        // Check for PlayerPoints
        if (Bukkit.getPluginManager().getPlugin("PlayerPoints") != null) {
            playerPointsAPI = PlayerPoints.getPlugin(PlayerPoints.class).getAPI();
        }

        String preferredCurrency = plugin.getConfig().getString("default-currency", "VAULT").toUpperCase();

        if (preferredCurrency.equals("VAULT") && vaultEconomy != null) {
            activeCurrency = CurrencyType.VAULT;
            plugin.getLogger().info("Using Vault for economy.");
            return true;
        }

        if (preferredCurrency.equals("PLAYER_POINTS") && playerPointsAPI != null) {
            activeCurrency = CurrencyType.PLAYER_POINTS;
            plugin.getLogger().info("Using PlayerPoints for economy.");
            return true;
        }

        // Fallback
        if (vaultEconomy != null) {
            activeCurrency = CurrencyType.VAULT;
            plugin.getLogger().info("Default currency provider not found. Falling back to Vault.");
            return true;
        }

        if (playerPointsAPI != null) {
            activeCurrency = CurrencyType.PLAYER_POINTS;
            plugin.getLogger().info("Default currency provider not found. Falling back to PlayerPoints.");
            return true;
        }

        activeCurrency = CurrencyType.NONE;
        plugin.getLogger().severe("No economy provider (Vault or PlayerPoints) found. The plugin will be disabled.");
        return false;
    }

    /**
     * Checks if a player has enough currency.
     *
     * @param player The player to check.
     * @param amount The amount to check for.
     * @return True if the player has enough, false otherwise.
     */
    public boolean hasEnough(Player player, double amount) {
        switch (activeCurrency) {
            case VAULT:
                return vaultEconomy.has(player, amount);
            case PLAYER_POINTS:
                return playerPointsAPI.look(player.getUniqueId()) >= amount;
            default:
                return false;
        }
    }

    /**
     * Withdraws currency from a player's account.
     *
     * @param player The player to withdraw from.
     * @param amount The amount to withdraw.
     * @return True if the transaction was successful, false otherwise.
     */
    public boolean withdraw(Player player, double amount) {
        switch (activeCurrency) {
            case VAULT:
                return vaultEconomy.withdrawPlayer(player, amount).transactionSuccess();
            case PLAYER_POINTS:
                return playerPointsAPI.take(player.getUniqueId(), (int) Math.round(amount));
            default:
                return false;
        }
    }

    /**
     * Gets the currently active currency type.
     *
     * @return The active CurrencyType.
     */
    public CurrencyType getActiveCurrency() {
        return activeCurrency;
    }
}
