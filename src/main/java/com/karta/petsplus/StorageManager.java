package com.karta.petsplus;

import com.karta.petsplus.storage.MySQLStorage;
import com.karta.petsplus.storage.SQLiteStorage;
import com.karta.petsplus.storage.Storage;
import com.karta.petsplus.storage.YamlStorage;

public class StorageManager {

    private final PetsPlus plugin;
    private Storage storage;

    public StorageManager(PetsPlus plugin) {
        this.plugin = plugin;
    }

    public void init() {
        String storageType = plugin.getConfig().getString("storage.type", "YAML").toUpperCase();
        switch (storageType) {
            case "MYSQL":
                storage = new MySQLStorage(plugin);
                break;
            case "SQLITE":
                storage = new SQLiteStorage(plugin);
                break;
            case "YAML":
            default:
                storage = new YamlStorage(plugin);
                break;
        }
        plugin.getLogger().info("Using " + storageType + " for data storage.");
        storage.init();
    }

    public Storage getStorage() {
        return storage;
    }

    public void shutdown() {
        if (storage != null) {
            storage.shutdown();
        }
    }
}
