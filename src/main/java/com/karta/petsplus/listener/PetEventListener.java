package com.karta.petsplus.listener;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.event.PetRenameEvent;
import com.karta.petsplus.manager.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class PetEventListener implements Listener {

    private final KartaPetsPlus plugin;
    private final ConfigManager configManager;

    public PetEventListener(KartaPetsPlus plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    @EventHandler
    public void onRename(PetRenameEvent event) {
        if (!configManager.isRenameEnabled()) {
            event.setCancelled(true);
            return;
        }

        String name = event.getNewName();
        if (name == null) {
            return;
        }

        Player player = event.getPlayer();

        if (configManager.isRenameTrim()) {
            name = name.trim();
        }

        if (!player.hasPermission("kartapetsplus.rename.bypass")) {
            String rawPattern = configManager.getRenameBlockedPattern();
            if (rawPattern != null && !rawPattern.isEmpty()) {
                if (name.matches(rawPattern)) {
                    event.setCancelled(true);
                    return;
                }
            }

            List<String> blockedWords = configManager.getRenameBlockedWords();
            if (!blockedWords.isEmpty()) {
                for (String word : blockedWords) {
                    if (name.toLowerCase().contains(word.toLowerCase())) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

        Component component = MiniMessage.miniMessage().deserialize(name);

        if (configManager.isRenameColorEnabled()) {
            if (!player.hasPermission("kartapetsplus.rename.color")) {
                name = PlainTextComponentSerializer.plainText().serialize(component);
            } else {
                name = LegacyComponentSerializer.legacyAmpersand().serialize(component);
            }
        } else {
            name = PlainTextComponentSerializer.plainText().serialize(component);
        }


        if (configManager.isRenameLimitCharsEnabled()) {
            int limit = configManager.getRenameLimitCharsNumber();
            if (name.length() > limit) {
                name = name.substring(0, limit);
            }
        }

        event.setNewName(name);
    }
}
