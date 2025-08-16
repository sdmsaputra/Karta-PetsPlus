package com.karta.petsplus.manager;

import com.karta.petsplus.KartaPetsPlus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import com.karta.petsplus.KartaPetsPlus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;

public class MessageManager {

    private final KartaPetsPlus plugin;
    private final String prefix;

    public MessageManager(KartaPetsPlus plugin) {
        this.plugin = plugin;
        this.prefix = plugin.getConfigManager().getConfig().getString("prefix", "<dark_gray>[<aqua>Pets</aqua>]</dark_gray> ");
    }

    public void sendMessage(CommandSender sender, String path, String defaultMessage) {
        String message = plugin.getConfigManager().getMessages().getString(path, defaultMessage);
        sender.sendMessage(MiniMessage.miniMessage().deserialize(prefix + message));
    }

    public void sendMessage(CommandSender sender, String path, String defaultMessage, TagResolver... placeholders) {
        String message = plugin.getConfigManager().getMessages().getString(path, defaultMessage);
        sender.sendMessage(MiniMessage.miniMessage().deserialize(prefix + message, placeholders));
    }

}
