package com.karta.petsplus.ui;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.data.Pet;
import com.karta.petsplus.manager.ConfigManager;
import com.karta.petsplus.manager.PlayerDataManager;
import com.karta.petsplus.ui.lib.PagedGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class PetManagementGUI extends PagedGUI {

    private final KartaPetsPlus plugin;
    private final Player player;

    private static final int PETS_PER_PAGE = 45;

    public PetManagementGUI(KartaPetsPlus plugin, Player player, int page) {
        super(plugin,
              plugin.getGuiManager().getGuiConfig().getString("pet-menu.title", "My Pets"),
              6,
              page,
              (int) Math.ceil((double) plugin.getPlayerDataManager().getPets(player).size() / PETS_PER_PAGE));
        this.plugin = plugin;
        this.player = player;
        populateItems();
    }

    private void populateItems() {
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        List<Pet> pets = playerDataManager.getPets(player);

        int startIndex = page * PETS_PER_PAGE;
        int endIndex = Math.min(startIndex + PETS_PER_PAGE, pets.size());

        for (int i = startIndex; i < endIndex; i++) {
            inventory.addItem(createPetItem(pets.get(i)));
        }

        String fillMaterialName = plugin.getGuiManager().getGuiConfig().getString("pet-menu.fill-item.material", "GRAY_STAINED_GLASS_PANE");
        fillBackground(Material.getMaterial(fillMaterialName));
        addNavigationButtons();
    }

    private ItemStack createPetItem(Pet pet) {
        ConfigManager configManager = plugin.getConfigManager();
        String petType = pet.getPetType();
        String iconName = configManager.getPets().getString("pets." + petType + ".icon", "BARRIER").toUpperCase();
        Material iconMaterial = Material.getMaterial(iconName);
        if (iconMaterial == null) {
            iconMaterial = Material.BARRIER;
        }

        ItemStack item = new ItemStack(iconMaterial);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.displayName(MiniMessage.miniMessage().deserialize(pet.getPetName()));

        List<Component> lore = new ArrayList<>();
        String petTypeName = configManager.getPets().getString("pets." + petType + ".display-name", petType);
        lore.add(MiniMessage.miniMessage().deserialize("<gray>Type: " + petTypeName));
        lore.add(Component.text(""));
        lore.add(MiniMessage.miniMessage().deserialize("<gray>Status: <yellow>" + pet.getStatus().name() + "</yellow>"));
        lore.add(Component.text(""));
        lore.add(MiniMessage.miniMessage().deserialize("<aqua>Left-click to summon/stow.</aqua>"));
        lore.add(MiniMessage.miniMessage().deserialize("<aqua>Right-click to rename.</aqua>"));
        meta.lore(lore);

        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "pet_uuid"), PersistentDataType.STRING, pet.getPetId().toString());
        item.setItemMeta(meta);

        return item;
    }

    public static void open(KartaPetsPlus plugin, Player player, int page) {
        if (plugin.getPlayerDataManager().getPets(player).isEmpty()) {
            plugin.getMessageManager().sendMessage(player, "no-pets-owned", "<red>You do not own any pets.</red>");
            return;
        }
        PetManagementGUI gui = new PetManagementGUI(plugin, player, page);
        player.openInventory(gui.getInventory());
    }
}
