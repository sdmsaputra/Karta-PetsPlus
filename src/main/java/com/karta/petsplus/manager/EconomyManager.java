package com.karta.petsplus.manager;

import com.karta.petsplus.KartaPetsPlus;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Manages the integration with economy plugins (PlayerPoints and Vault).
 * This class handles detecting and hooking into the available economy providers.
 */
public class EconomyManager {

    private final KartaPetsPlus plugin;
    private Economy vaultEconomy = null;
    private PlayerPointsAPI playerPointsAPI = null;
    private String activeEconomyProvider = "None";

    public EconomyManager(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    /**
     * Sets up the economy integration by checking for PlayerPoints and Vault.
     * PlayerPoints is checked first, and if not found, it falls back to Vault.
     *
     * @return true if a supported economy provider was found, false otherwise.
     */
    public boolean setupEconomy() {
        // Try to hook into PlayerPoints first
        if (plugin.getServer().getPluginManager().getPlugin("PlayerPoints") != null) {
            try {
                playerPointsAPI = PlayerPoints.getPlugin(PlayerPoints.class).getAPI();
                activeEconomyProvider = "PlayerPoints";
                plugin.getLogger().info("Successfully hooked into PlayerPoints for economy.");
                return true;
            } catch (Exception e) {
                plugin.getLogger().warning("Found PlayerPoints, but failed to hook into its API. Falling back to Vault.");
            }
        }

        // Fallback to Vault
        if (plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                vaultEconomy = rsp.getProvider();
                activeEconomyProvider = "Vault";
                plugin.getLogger().info("Successfully hooked into Vault for economy.");
                return true;
            }
        }

        return false;
    }

    /**
     * Gets the balance of a player.
     *
     * @param player The player to check.
     * @return The player's balance.
     */
    public double getBalance(Player player) {
        if ("PlayerPoints".equals(activeEconomyProvider) && playerPointsAPI != null) {
            return playerPointsAPI.look(player.getUniqueId());
        } else if ("Vault".equals(activeEconomyProvider) && vaultEconomy != null) {
            return vaultEconomy.getBalance(player);
        }
        return 0.0;
    }

    /**
     * Checks if a player has enough funds.
     *
     * @param player The player to check.
     * @param amount The amount to check for.
     * @return true if the player has enough funds, false otherwise.
     */
    public boolean has(Player player, double amount) {
        return getBalance(player) >= amount;
    }

    /**
     * Withdraws funds from a player's account.
     *
     * @param player The player to withdraw from.
     * @param amount The amount to withdraw.
     * @return true if the transaction was successful, false otherwise.
     */
    public boolean withdraw(Player player, double amount) {
        if ("PlayerPoints".equals(activeEconomyProvider) && playerPointsAPI != null) {
            return playerPointsAPI.take(player.getUniqueId(), (int) Math.round(amount));
        } else if ("Vault".equals(activeEconomyProvider) && vaultEconomy != null) {
            return vaultEconomy.withdrawPlayer(player, amount).transactionSuccess();
        }
        return false;
    }

    /**
     * Formats the given amount using the active economy provider.
     *
     * @param amount The amount to format.
     * @return The formatted currency string.
     */
    public String format(double amount) {
        if ("PlayerPoints".equals(activeEconomyProvider)) {
            return String.format("%d Points", (int) Math.round(amount));
        } else if ("Vault".equals(activeEconomyProvider) && vaultEconomy != null) {
            return vaultEconomy.format(amount);
        }
        // Fallback for when no economy provider is available
        return String.format("%.2f", amount);
    }

    /**
     * Gets the name of the active economy provider.
     *
     * @return A string representing the active economy provider ("PlayerPoints", "Vault", or "None").
     */
    public String getActiveEconomyProvider() {
        return activeEconomyProvider;
    }
}
