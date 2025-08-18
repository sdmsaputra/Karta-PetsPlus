package com.karta.petsplus.shop;

import com.karta.petsplus.PetType;
import com.karta.petsplus.PetsPlus;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IconResolver {

    private final PetsPlus plugin;
    private final ShopConfig shopConfig;
    private final DecimalFormat priceFormatter = new DecimalFormat("#,###.##");

    public IconResolver(PetsPlus plugin, ShopConfig shopConfig) {
        this.plugin = plugin;
        this.shopConfig = shopConfig;
    }

    public ItemStack createPetIcon(PetType petType, boolean isUnlocked, boolean isPurchasable, double price, CurrencyProvider currency) {
        ConfigurationSection defaults = shopConfig.getDefaultPetSection();
        ConfigurationSection override = shopConfig.getOverride(petType.getInternalName());

        Material material = resolveMaterial(petType, override, defaults);
        String name = resolveString("name", override, defaults);
        List<String> lore = resolveStringList("lore", override, defaults);

        ItemStack icon = new ItemStack(material);
        ItemMeta meta = icon.getItemMeta();

        if (meta != null) {
            String status;
            if (!isPurchasable) {
                status = "&cUnavailable"; // Or make this configurable
            } else if (isUnlocked) {
                status = "&aUnlocked";
            } else {
                status = "&eClick to Buy";
            }

            String formattedPrice = shopConfig.shouldFormatPrice() ? priceFormatter.format(price) : String.valueOf(price);

            meta.setDisplayName(replacePlaceholders(name, petType, status, formattedPrice, currency));
            meta.setLore(lore.stream()
                    .map(line -> replacePlaceholders(line, petType, status, formattedPrice, currency))
                    .collect(Collectors.toList()));
            icon.setItemMeta(meta);
        }

        return icon;
    }

    private String replacePlaceholders(String text, PetType petType, String status, String price, CurrencyProvider currency) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text
                .replace("%pet_display_name%", petType.getDisplayName())
                .replace("%pet_type%", petType.getInternalName())
                .replace("%pet_price%", price)
                .replace("%currency_symbol%", currency.getCurrencySymbol())
                .replace("%currency_name%", currency.getCurrencyName())
                .replace("%pet_status%", status)
                .replace("%pet_description%", petType.getDescription() != null ? petType.getDescription() : "")
        );
    }

    private Material resolveMaterial(PetType petType, ConfigurationSection override, ConfigurationSection defaults) {
        String materialName = null;
        if (override != null && override.isString("material")) {
            materialName = override.getString("material");
        }

        if (materialName == null) {
            // Try to find a spawn egg for the entity type
            EntityType entityType = petType.getEntityType();
            Material spawnEgg = Material.getMaterial(entityType.name() + "_SPAWN_EGG");
            if (spawnEgg != null) {
                return spawnEgg;
            }
        }

        if (materialName == null) {
            materialName = defaults.getString("material", "PLAYER_HEAD");
        }

        try {
            return Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material in shop.yml: " + materialName + ". Defaulting to PLAYER_HEAD.");
            return Material.PLAYER_HEAD;
        }
    }

    private String resolveString(String path, ConfigurationSection override, ConfigurationSection defaults) {
        if (override != null && override.isString(path)) {
            return override.getString(path);
        }
        return defaults.getString(path, "");
    }

    private List<String> resolveStringList(String path, ConfigurationSection override, ConfigurationSection defaults) {
        if (override != null && override.isList(path)) {
            return override.getStringList(path);
        }
        return defaults.getStringList(path);
    }
}
