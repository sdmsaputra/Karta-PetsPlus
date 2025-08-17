package com.karta.petsplus.ui;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.manager.ConfigManager;
import com.karta.petsplus.manager.PlayerDataManager;
import com.karta.petsplus.ui.lib.PagedGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class PetShopGUI extends PagedGUI {

    private final Player player;
    private final KartaPetsPlus plugin;

    private static final int PETS_PER_PAGE = 45;

    public PetShopGUI(KartaPetsPlus plugin, Player player, int page) {
        super(plugin,
              plugin.getConfigManager().getGui().getString("pet-shop.title", "<light_purple><bold>KartaPetsPlus</bold></light_purple>"),
              6, // 6 rows
              page,
              (int) Math.ceil((double) getPetIds(plugin).size() / PETS_PER_PAGE));
        this.plugin = plugin;
        this.player = player;
        populateItems();
    }

    private void populateItems() {
        List<String> petIds = getPetIds(plugin);
        int startIndex = page * PETS_PER_PAGE;
        int endIndex = Math.min(startIndex + PETS_PER_PAGE, petIds.size());

        ConfigManager configManager = plugin.getConfigManager();
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        Set<String> ownedPets = playerDataManager.getPets(player).stream()
                                                  .map(p -> p.getPetType().name().toUpperCase())
                                                  .collect(Collectors.toSet());

        for (int i = startIndex; i < endIndex; i++) {
            String petId = petIds.get(i);
            String petPath = "pets." + petId;

            ItemStack item = createPetIcon(petPath);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            String displayName = configManager.getPets().getString(petPath + ".display-name", "<red>Unknown Pet</red>");
            meta.displayName(MiniMessage.miniMessage().deserialize("<!i>" + displayName));

            double price = configManager.getPets().getDouble(petPath + ".price", 0.0);
            String formattedPrice = plugin.getEconomyManager().format(price);
            boolean isOwned = ownedPets.contains(petId.toUpperCase());

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(""));
            lore.add(Component.text("Price: ", NamedTextColor.GRAY).append(Component.text(formattedPrice, NamedTextColor.GOLD)));
            lore.add(Component.text("Status: ", NamedTextColor.GRAY).append(isOwned
                    ? Component.text("Owned", NamedTextColor.GREEN)
                    : Component.text("Not Owned", NamedTextColor.RED)));
            lore.add(Component.text(""));
            if (isOwned) {
                lore.add(Component.text("Right-click to summon!", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, true));
            } else {
                lore.add(Component.text("Left-click to purchase!", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, true));
            }

            meta.lore(lore);
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "pet_id"), PersistentDataType.STRING, petId);
            item.setItemMeta(meta);

            inventory.addItem(item);
        }

        addCustomNavigation();
    }

    private ItemStack createPetIcon(String petPath) {
        ConfigManager configManager = plugin.getConfigManager();
        String headTexture = configManager.getPets().getString(petPath + ".head-texture");

        if (headTexture != null && !headTexture.isEmpty()) {
            return createPlayerHead(headTexture);
        } else {
            String iconName = configManager.getPets().getString(petPath + ".icon", "BARRIER").toUpperCase();
            Material iconMaterial = Material.getMaterial(iconName);
            if (iconMaterial == null) {
                iconMaterial = Material.BARRIER;
            }
            return new ItemStack(iconMaterial);
        }
    }

    private ItemStack createPlayerHead(String texture) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) return head;

        com.destroystokyo.paper.profile.PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.getProperties().add(new ProfileProperty("textures", texture));
        meta.setPlayerProfile(profile);

        head.setItemMeta(meta);
        return head;
    }

    private void addCustomNavigation() {
        // Filler
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.displayName(Component.text(" "));
        filler.setItemMeta(fillerMeta);
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, filler);
        }

        // Next Page
        if (page < totalPages - 1) {
            ItemStack next = new ItemStack(Material.EMERALD);
            ItemMeta nextMeta = next.getItemMeta();
            nextMeta.displayName(Component.text("Next Page", NamedTextColor.GREEN));
            nextMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "gui_action"), PersistentDataType.STRING, "next_page");
            next.setItemMeta(nextMeta);
            inventory.setItem(47, next);
        }

        // Close Button
        ItemStack close = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("Close", NamedTextColor.RED));
        closeMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "gui_action"), PersistentDataType.STRING, "close");
        close.setItemMeta(closeMeta);
        inventory.setItem(49, close);

        // My Pets Button
        ItemStack myPets = new ItemStack(Material.DIAMOND_HELMET);
        ItemMeta myPetsMeta = myPets.getItemMeta();
        myPetsMeta.displayName(Component.text("My Pets", NamedTextColor.AQUA));
        myPetsMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "gui_action"), PersistentDataType.STRING, "my_pets");
        myPets.setItemMeta(myPetsMeta);
        inventory.setItem(51, myPets);
    }

    public static void openShop(KartaPetsPlus plugin, Player player, int page) {
        if (getPetIds(plugin).isEmpty()) {
            plugin.getMessageManager().sendMessage(player, "shop-empty", "<red>The pet shop is currently empty.</red>");
            return;
        }
        PetShopGUI shopGUI = new PetShopGUI(plugin, player, page);
        player.openInventory(shopGUI.getInventory());
    }

    private static List<String> getPetIds(KartaPetsPlus plugin) {
        ConfigurationSection petsSection = plugin.getConfigManager().getPets().getConfigurationSection("pets");
        if (petsSection == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(petsSection.getKeys(false));
    }
}
