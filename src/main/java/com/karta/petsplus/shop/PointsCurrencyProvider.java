package com.karta.petsplus.shop;

import com.karta.petsplus.PetsPlus;
import com.karta.petsplus.api.points.PlayerPoints;
import com.karta.petsplus.api.points.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PointsCurrencyProvider implements CurrencyProvider {

    private PlayerPointsAPI pointsApi;

    public PointsCurrencyProvider(PetsPlus plugin) {
        setupPlayerPoints();
    }

    private void setupPlayerPoints() {
        Plugin pointsPlugin = Bukkit.getPluginManager().getPlugin("PlayerPoints");
        if (pointsPlugin instanceof PlayerPoints) {
            this.pointsApi = ((PlayerPoints) pointsPlugin).getAPI();
        } else {
            this.pointsApi = null;
        }
    }

    public boolean isEnabled() {
        return this.pointsApi != null;
    }

    @Override
    public String getInternalName() {
        return "POINTS";
    }

    @Override
    public String getCurrencyName() {
        return "Points";
    }

    @Override
    public String getCurrencySymbol() {
        return "pts";
    }

    @Override
    public boolean has(Player player, double amount) {
        if (!isEnabled()) return false;
        return pointsApi.look(player.getUniqueId()) >= amount;
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        if (!isEnabled() || amount < 0) return false;
        return pointsApi.takePoints(player.getUniqueId(), (int) Math.round(amount));
    }

    @Override
    public double getBalance(Player player) {
        if (!isEnabled()) return 0;
        return pointsApi.look(player.getUniqueId());
    }
}
