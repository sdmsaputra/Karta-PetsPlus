package com.karta.petsplus.command.subcommand;

import com.karta.petsplus.KartaPetsPlus;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class GenerateMobsCommand implements SubCommand {

    private final KartaPetsPlus plugin;

    public GenerateMobsCommand(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "generatemobs";
    }

    @Override
    public String getDescription() {
        return "Generates a basic configuration for all missing mobs in pets.yml.";
    }

    @Override
    public String getSyntax() {
        return "/pets generatemobs";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!sender.hasPermission("kartapetsplus.admin.generatemobs")) {
            plugin.getMessageManager().sendMessage(sender, "no-permission", "<red>You do not have permission to use this command.</red>");
            return;
        }

        plugin.getMessageManager().sendMessage(sender, "generating-mobs", "<yellow>Generating mob configurations... This may take a moment.</yellow>");

        ConfigurationSection petsSection = plugin.getConfigManager().getPets().getConfigurationSection("pets");
        if (petsSection == null) {
            petsSection = plugin.getConfigManager().getPets().createSection("pets");
        }
        Set<String> existingPets = petsSection.getKeys(false);
        int newPetsCount = 0;

        for (EntityType type : EntityType.values()) {
            if (type.isAlive() && type.isSpawnable() && type.getName() != null) {
                String mobName = type.name().toLowerCase();
                if (!existingPets.contains(mobName)) {
                    petsSection.set(mobName + ".display-name", "<yellow>" + capitalize(type.name()) + " Pet</yellow>");
                    petsSection.set(mobName + ".icon", "STONE");
                    petsSection.set(mobName + ".price", 1000.0);
                    petsSection.set(mobName + ".entity-type", type.name());
                    petsSection.set(mobName + ".abilities", Collections.singletonList("NONE"));
                    newPetsCount++;
                }
            }
        }

        plugin.getConfigManager().savePets();
        plugin.getMessageManager().sendMessage(sender, "mobs-generated", "<green>Successfully generated " + newPetsCount + " new mob configurations in pets.yml.</green>");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase().replace("_", " ");
    }
}
