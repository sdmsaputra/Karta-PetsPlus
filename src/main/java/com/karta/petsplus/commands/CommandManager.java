package com.karta.petsplus.commands;

import com.karta.petsplus.PetsPlus;

public class CommandManager {

    private final PetsPlus plugin;

    public CommandManager(PetsPlus plugin) {
        this.plugin = plugin;
    }

    public void registerCommands() {
        plugin.getCommand("pets").setExecutor(new PetsCommand(plugin));
    }
}
