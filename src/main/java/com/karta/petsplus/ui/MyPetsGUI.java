package com.karta.petsplus.ui;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.data.Pet;
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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class MyPetsGUI extends PagedGUI {

    private final Player player;
    private final KartaPetsPlus plugin;

    private static final int PETS_PER_PAGE = 45;

    public MyPetsGUI(KartaPetsPlus plugin, Player player, int page) {
        super(plugin,
              "§d§lMy Pets",
              6, // 6 rows
              page,
              (int) Math.ceil((double) plugin.getPlayerDataManager().getPets(player).size() / PETS_PER_PAGE));
        this.plugin = plugin;
        this.player = player;
        populateItems();
    }

    private void populateItems() {
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        List<Pet> ownedPets = new ArrayList<>(playerDataManager.getPets(player));

        int startIndex = page * PETS_PER_PAGE;
        int endIndex = Math.min(startIndex + PETS_PER_PAGE, ownedPets.size());

        for (int i = startIndex; i < endIndex; i++) {
            Pet pet = ownedPets.get(i);
            String petId = pet.getPetType().name();
            String petPath = "pets." + petId.toLowerCase();

            ItemStack item = createPetIcon(petPath);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            String displayName = plugin.getConfigManager().getPets().getString(petPath + ".display-name", "<red>Unknown Pet</red>");
            meta.displayName(MiniMessage.miniMessage().deserialize("<!i>" + displayName));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(""));
            lore.add(Component.text("Status: ", NamedTextColor.GRAY).append(
                pet.getStatus() == Pet.PetStatus.SUMMONED || pet.getStatus() == Pet.PetStatus.STAY
                    ? Component.text("Summoned", NamedTextColor.GREEN)
                    : Component.text("Stowed", NamedTextColor.GRAY)
            ));
            lore.add(Component.text(""));
            lore.add(Component.text("Click to summon!", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, true));

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

        // Back to Shop Button
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.displayName(Component.text("Back to Pet Shop", NamedTextColor.GREEN));
        backMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "gui_action"), PersistentDataType.STRING, "open_shop");
        backButton.setItemMeta(backMeta);
        inventory.setItem(49, backButton);
    }

    public static void open(KartaPetsPlus plugin, Player player, int page) {
        if (plugin.getPlayerDataManager().getPets(player).isEmpty()) {
            plugin.getMessageManager().sendMessage(player, "no-pets-owned", "<red>You do not own any pets yet.</red>");
            return;
        }
        MyPetsGUI myPetsGUI = new MyPetsGUI(plugin, player, page);
        player.openInventory(myPetsGUI.getInventory());
    }
}
