package com.karta.petsplus.manager;

import com.karta.petsplus.KartaPetsPlus;
import net.milkbowl.vault.economy.Economy;
// import org.black_ixx.playerpoints.PlayerPoints;
// import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Manages the integration with economy plugins (Vault and PlayerPoints).
 * This class handles detecting and hooking into the available economy providers.
 */
public class EconomyManager {

    private final KartaPetsPlus plugin;
    private Economy vaultEconomy = null;
    // private PlayerPointsAPI playerPointsAPI = null;
    private String activeEconomyProvider = "None";

    public EconomyManager(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    /**
     * Sets up the economy integration by checking for Vault and PlayerPoints.
     * Vault is checked first, and if not found, it falls back to PlayerPoints.
     *
     * @return true if a supported economy provider was found, false otherwise.
     */
    public boolean setupEconomy() {
        // Try to hook into Vault
        if (plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                vaultEconomy = rsp.getProvider();
                activeEconomyProvider = "Vault";
                plugin.getLogger().info("Successfully hooked into Vault for economy.");
                return true;
            }
        }

        // Fallback to PlayerPoints (temporarily disabled)
        // if (plugin.getServer().getPluginManager().getPlugin("PlayerPoints") != null) {
        //     try {
        //         playerPointsAPI = PlayerPoints.getPlugin(PlayerPoints.class).getAPI();
        //         activeEconomyProvider = "PlayerPoints";
        //         plugin.getLogger().info("Successfully hooked into PlayerPoints for economy.");
        //         return true;
        //     } catch (Exception e) {
        //         plugin.getLogger().warning("Found PlayerPoints, but failed to hook into its API.");
        //     }
        // }

        return vaultEconomy != null;
    }

    /**
     * Gets the name of the active economy provider.
     *
     * @return A string representing the active economy provider ("Vault", "PlayerPoints", or "None").
     */
    public String getActiveEconomyProvider() {
        return activeEconomyProvider;
    }

    /**
     * Gets the Vault Economy provider instance.
     *
     * @return The Economy instance, or null if Vault is not in use.
     */
    public Economy getVaultEconomy() {
        return vaultEconomy;
    }

    /**
     * Gets the PlayerPoints API instance.
     *
     * @return The PlayerPointsAPI instance, or null if PlayerPoints is not in use.
     */
    // public PlayerPointsAPI getPlayerPointsAPI() {
    //     return playerPointsAPI;
    // }

    /**
     * Formats the given amount using the active economy provider.
     *
     * @param amount The amount to format.
     * @return The formatted currency string.
     */
    public String format(double amount) {
        if ("Vault".equals(activeEconomyProvider) && vaultEconomy != null) {
            return vaultEconomy.format(amount);
        }
        // Fallback for when no economy provider is available
        return String.format("%.2f", amount);
    }
}
