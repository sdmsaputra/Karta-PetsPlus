package com.karta.petsplus.ui;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.manager.ConfigManager;
import com.karta.petsplus.manager.PlayerDataManager;
import com.karta.petsplus.ui.lib.PagedGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PetShopGUI extends PagedGUI {

    private final Player player;
    private final KartaPetsPlus plugin;

    private static final int PETS_PER_PAGE = 45;

    public PetShopGUI(KartaPetsPlus plugin, Player player, int page) {
        super(plugin,
              plugin.getGuiManager().getGuiConfig().getString("pet-shop.title", "Pet Shop"),
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
        List<String> ownedPets = playerDataManager.getPets(player).stream()
                                                  .map(p -> p.getPetType())
                                                  .collect(Collectors.toList());

        for (int i = startIndex; i < endIndex; i++) {
            String petId = petIds.get(i);
            String petPath = "pets." + petId;

            String iconName = configManager.getPets().getString(petPath + ".icon", "BARRIER").toUpperCase();
            Material iconMaterial = Material.getMaterial(iconName);
            if (iconMaterial == null) {
                iconMaterial = Material.BARRIER;
            }

            ItemStack item = new ItemStack(iconMaterial);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            String displayName = configManager.getPets().getString(petPath + ".display-name", "<red>Unknown Pet</red>");
            meta.displayName(MiniMessage.miniMessage().deserialize(displayName));

            double price = configManager.getPets().getDouble(petPath + ".price", 0.0);
            String formattedPrice = plugin.getEconomyManager().format(price);

            List<String> loreStrings = configManager.getPets().getStringList(petPath + ".lore");
            List<Component> lore = loreStrings.stream()
                    .map(line -> MiniMessage.miniMessage().deserialize("<!i>" + line.replace("{price}", formattedPrice)))
                    .collect(Collectors.toList());

            lore.add(Component.text("")); // Spacer

            if (ownedPets.contains(petId)) {
                lore.add(MiniMessage.miniMessage().deserialize("<red>You already own this pet!</red>"));
            } else {
                lore.add(MiniMessage.miniMessage().deserialize("<green>Click to purchase!</green>"));
            }

            meta.lore(lore);
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "pet_id"), PersistentDataType.STRING, petId);
            item.setItemMeta(meta);

            inventory.addItem(item);
        }

        String fillMaterialName = plugin.getGuiManager().getGuiConfig().getString("pet-shop.fill-item.material", "BLACK_STAINED_GLASS_PANE");
        fillBackground(Material.getMaterial(fillMaterialName));
        addNavigationButtons();
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
