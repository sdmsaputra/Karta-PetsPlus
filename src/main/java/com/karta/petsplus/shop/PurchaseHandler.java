package com.karta.petsplus.shop;

import com.karta.petsplus.MessageManager;
import com.karta.petsplus.PetData;
import com.karta.petsplus.PetType;
import com.karta.petsplus.PetsPlus;
import com.karta.petsplus.StorageManager;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class PurchaseHandler {

    private final PetsPlus plugin;
    private final ShopManager shopManager;
    private final StorageManager storageManager;
    private final MessageManager messageManager;
    private final ShopConfig shopConfig;

    private final Map<UUID, ReentrantLock> purchaseLocks = new ConcurrentHashMap<>();
    private final Map<UUID, Long> clickCooldowns = new ConcurrentHashMap<>();

    public PurchaseHandler(PetsPlus plugin, ShopManager shopManager, StorageManager storageManager, MessageManager messageManager, ShopConfig shopConfig) {
        this.plugin = plugin;
        this.shopManager = shopManager;
        this.storageManager = storageManager;
        this.messageManager = messageManager;
        this.shopConfig = shopConfig;
    }

    public void attemptPurchase(Player player, PetType petType, double price, CurrencyProvider currency) {
        UUID playerUuid = player.getUniqueId();

        // 1. Check click cooldown
        long now = System.currentTimeMillis();
        long lastClick = clickCooldowns.getOrDefault(playerUuid, 0L);
        if (now - lastClick < shopConfig.getClickCooldown()) {
            return; // Silently ignore spam clicks
        }
        clickCooldowns.put(playerUuid, now);

        // 2. Acquire lock to prevent concurrent purchases
        ReentrantLock lock = purchaseLocks.computeIfAbsent(playerUuid, k -> new ReentrantLock());
        if (!lock.tryLock()) {
            // messageManager.sendMessage(player, "shop_purchase_in_progress");
            return; // Purchase already in progress
        }

        try {
            // 3. Run validations
            if (!player.hasPermission("petsplus.buy") && !player.hasPermission("petsplus.buy." + petType.getInternalName())) {
                player.sendMessage(messageManager.getMessage("no-permission"));
                player.playSound(player.getLocation(), shopConfig.getInsufficientFundsSound(), 1, 1);
                return;
            }

            if (shopManager.getUnlockedPets(player).contains(petType.getInternalName())) {
                player.sendMessage(messageManager.getMessage("shop_already_owned", "%pet_display_name%", petType.getDisplayName()));
                return;
            }

            // 4. Check for confirmation screen
            boolean confirmEnabled = shopConfig.getConfirmSection().getBoolean("enabled", true);

            if (confirmEnabled) {
                // Open confirmation menu
                new PurchaseConfirmMenu(plugin, shopManager, player, petType, price, currency).open();
            } else {
                // No confirmation needed, proceed directly
                executePurchase(player, petType, price, currency);
            }

        } finally {
            // If we didn't open a confirm menu, the lock is released here.
            // If we did, the confirm menu is now responsible for releasing the lock.
            boolean confirmEnabled = shopConfig.getConfirmSection().getBoolean("enabled", true);
            if (!confirmEnabled) {
                lock.unlock();
            }
        }
    }

    public void executePurchase(Player player, PetType petType, double price, CurrencyProvider currency) {
        // This method should only be called after acquiring the lock.
        // The calling method is responsible for managing the lock's lifecycle.

        Runnable purchaseTask = () -> {
            // Perform economy operations
            boolean hasFunds = currency.has(player, price);

            if (!hasFunds) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.sendMessage(messageManager.getMessage("shop_insufficient_funds", "%currency_name%", currency.getCurrencyName()));
                    player.playSound(player.getLocation(), shopConfig.getInsufficientFundsSound(), 1, 1);
                    player.closeInventory();
                });
                return;
            }

            boolean withdrawn = currency.withdraw(player, price);

            if (withdrawn) {
                // Run storage and feedback on the main thread
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        storageManager.getStorage().unlockPet(player.getUniqueId(), petType.getInternalName()).thenAccept(v -> {
                            shopManager.addUnlockedPet(player, petType.getInternalName()); // Update shop cache

                            // Also update the main PetData cache
                            PetData petData = plugin.getPetManager().getPlayerData(player);
                            if (petData != null) {
                                petData.addOwnedPet(petType.getInternalName());
                            }

                            player.sendMessage(messageManager.getMessage("shop_purchase_success", "%pet_display_name%", petType.getDisplayName(), "%pet_price%", String.valueOf(price), "%currency_symbol%", currency.getCurrencySymbol()));
                            player.playSound(player.getLocation(), shopConfig.getSuccessSound(), 1, 1);
                            shopManager.refreshShopMenuFor(player); // Refresh the menu to show "Unlocked"
                        });
                    }
                }.runTask(plugin);
            } else {
                // Withdrawal failed for some reason (e.g., another plugin cancelled it)
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.sendMessage(messageManager.getMessage("shop_error"));
                        player.playSound(player.getLocation(), shopConfig.getInsufficientFundsSound(), 1, 1);
                        player.closeInventory();
                    }
                }.runTask(plugin);
            }
        };

        // Run the task async or sync based on config
        if (shopConfig.isAsyncEconomy()) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, purchaseTask);
        } else {
            purchaseTask.run();
        }

        // The calling method is now responsible for releasing the lock.
    }

    public ReentrantLock getPurchaseLock(UUID playerUuid) {
        return purchaseLocks.get(playerUuid);
    }
}
