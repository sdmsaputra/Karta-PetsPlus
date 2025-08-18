package com.karta.petsplus.shop;

import com.karta.petsplus.MessageManager;
import com.karta.petsplus.PetType;
import com.karta.petsplus.PetsPlus;
import com.karta.petsplus.StorageManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class ShopManager implements Listener {

    private final PetsPlus plugin;
    private final StorageManager storageManager;
    private final MessageManager messageManager;

    private ShopConfig shopConfig;
    private IconResolver iconResolver;
    private PurchaseHandler purchaseHandler;

    private final Map<String, CurrencyProvider> currencyProviders = new HashMap<>();
    private CurrencyProvider defaultCurrencyProvider;
    private final Map<UUID, Set<String>> unlockedPetsCache = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> activePreviews = new ConcurrentHashMap<>();

    public ShopManager(PetsPlus plugin, StorageManager storageManager, MessageManager messageManager) {
        this.plugin = plugin;
        this.storageManager = storageManager;
        this.messageManager = messageManager;
    }

    public void init() {
        this.shopConfig = new ShopConfig(plugin);
        this.iconResolver = new IconResolver(plugin, shopConfig);
        this.purchaseHandler = new PurchaseHandler(plugin, this, storageManager, messageManager, shopConfig);
        loadCurrencyProviders();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Load data for any players who are already online (e.g., on a /reload)
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            loadPlayerPetCache(player.getUniqueId());
        }
    }

    public void reload() {
        shopConfig.load();
        loadCurrencyProviders();
        plugin.getLogger().info("Pet Shop configuration reloaded.");
    }

    private void loadCurrencyProviders() {
        currencyProviders.clear();

        VaultCurrencyProvider vaultProvider = new VaultCurrencyProvider(plugin);
        if (vaultProvider.isEnabled()) {
            currencyProviders.put("VAULT", vaultProvider);
            plugin.getLogger().log(Level.INFO, "Vault currency provider enabled.");
        }

        PointsCurrencyProvider pointsProvider = new PointsCurrencyProvider(plugin);
        if (pointsProvider.isEnabled()) {
            currencyProviders.put("POINTS", pointsProvider);
            plugin.getLogger().log(Level.INFO, "PlayerPoints currency provider enabled.");
        }

        TokenCurrencyProvider tokenProvider = new TokenCurrencyProvider(plugin);
        // Assuming TokenCurrencyProvider has an isEnabled method
        // currencyProviders.put("TOKENS", tokenProvider);

        // Fallback logic
        if (currencyProviders.containsKey("VAULT")) {
            defaultCurrencyProvider = currencyProviders.get("VAULT");
        } else if (currencyProviders.containsKey("POINTS")) {
            defaultCurrencyProvider = currencyProviders.get("POINTS");
        } else if (currencyProviders.containsKey("TOKENS")) {
            defaultCurrencyProvider = currencyProviders.get("TOKENS");
        } else {
            defaultCurrencyProvider = null;
            plugin.getLogger().warning("No economy providers found! The pet shop will be disabled.");
        }
    }

    public void openShop(Player player) {
        if (defaultCurrencyProvider == null) {
            // messageManager.sendMessage(player, "shop_disabled_no_economy");
            return;
        }
        new ShopMenu(plugin, this, player).open();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        loadPlayerPetCache(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        unlockedPetsCache.remove(uuid);
        stopPetPreview(event.getPlayer());
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().equals(plugin)) {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                stopPetPreview(player);
            }
        }
    }

    private void loadPlayerPetCache(UUID uuid) {
        storageManager.getStorage().getUnlockedPets(uuid).thenAccept(pets -> {
            unlockedPetsCache.put(uuid, new ConcurrentHashMap<String, Boolean>().keySet(true));
            unlockedPetsCache.get(uuid).addAll(pets);
        });
    }

    public void refreshShopMenuFor(Player player) {
        Inventory openInventory = player.getOpenInventory().getTopInventory();
        if (openInventory.getHolder() instanceof ShopMenu shopMenu) {
            shopMenu.open(); // Re-opens and re-renders
        }
    }

    public void startPetPreview(Player player, PetType petType) {
        // Stop any existing preview first to prevent multiple previews
        stopPetPreview(player);

        ConfigurationSection previewConfig = shopConfig.getDefaultPetSection().getConfigurationSection("preview");
        if (previewConfig == null || !previewConfig.getBoolean("enabled", true)) {
            return;
        }

        long duration = previewConfig.getLong("duration-seconds", 10);
        if (duration <= 0) return;

        Entity petEntity = player.getWorld().spawnEntity(player.getLocation(), petType.getEntityType());

        // Gracefully fail if spawning is cancelled by another plugin (e.g., WorldGuard)
        if (petEntity == null) {
            player.sendMessage(messageManager.getMessage("shop_preview_failed_region"));
            return;
        }

        petEntity.setInvulnerable(true);
        petEntity.setSilent(true);
        petEntity.setMetadata("petsplus_preview", new FixedMetadataValue(plugin, true));

        if (petEntity instanceof Mob mob) {
            mob.setAI(false); // Prevents the pet from wandering off
        }

        player.sendMessage(messageManager.getMessage("shop_preview_start", "%pet_display_name%", petType.getDisplayName(), "%seconds%", String.valueOf(duration)));

        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (petEntity.isValid()) {
                petEntity.remove();
            }
            player.sendMessage(messageManager.getMessage("shop_preview_end"));
            activePreviews.remove(player.getUniqueId());
        }, duration * 20L); // Convert seconds to ticks

        activePreviews.put(player.getUniqueId(), task);
    }

    public void stopPetPreview(Player player) {
        if (activePreviews.containsKey(player.getUniqueId())) {
            activePreviews.get(player.getUniqueId()).cancel();
            activePreviews.remove(player.getUniqueId());
        }
        for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
            if (entity.hasMetadata("petsplus_preview")) {
                entity.remove();
            }
        }
    }

    // --- Getters ---
    public ShopConfig getShopConfig() { return shopConfig; }
    public IconResolver getIconResolver() { return iconResolver; }
    public PurchaseHandler getPurchaseHandler() { return purchaseHandler; }
    public Optional<CurrencyProvider> getCurrencyProvider(String name) { return Optional.ofNullable(currencyProviders.get(name.toUpperCase())); }
    public CurrencyProvider getDefaultCurrencyProvider() { return defaultCurrencyProvider; }
    public Set<String> getUnlockedPets(Player player) { return unlockedPetsCache.getOrDefault(player.getUniqueId(), Collections.emptySet()); }
    public void addUnlockedPet(Player player, String petType) {
        unlockedPetsCache.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<String, Boolean>().keySet(true)).add(petType.toLowerCase());
    }
}
