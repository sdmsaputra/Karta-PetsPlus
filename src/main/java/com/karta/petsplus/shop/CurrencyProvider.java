package com.karta.petsplus.shop;

import org.bukkit.entity.Player;

public interface CurrencyProvider {
    /**
     * The internal name of the currency provider (e.g., "VAULT").
     * @return The name.
     */
    String getInternalName();

    /**
     * The display name of the currency (e.g., "Coins").
     * @return The display name.
     */
    String getCurrencyName();

    /**
     * The symbol for the currency (e.g., "$").
     * @return The currency symbol.
     */
    String getCurrencySymbol();

    /**
     * Check if a player has at least a certain amount of the currency.
     * @param player The player to check.
     * @param amount The amount to check for.
     * @return True if the player has enough, false otherwise.
     */
    boolean has(Player player, double amount);

    /**
     * Withdraw a certain amount of the currency from a player's balance.
     * @param player The player to withdraw from.
     * @param amount The amount to withdraw.
     * @return True if the withdrawal was successful, false otherwise.
     */
    boolean withdraw(Player player, double amount);

    /**
     * Get the player's balance.
     * @param player The player to check.
     * @return The player's balance.
     */
    double getBalance(Player player);
}
