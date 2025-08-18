# KartaPetsPlus

**Version: 1.0.0**

KartaPetsPlus is a powerful pets plugin for PaperMC servers. It allows players to purchase pets through a GUI shop and manage them.

## Features

- **Pet Ownership**: Players can buy pets from a shop.
- **GUI Shop**: An in-game GUI for browsing and purchasing pets.
- **Economy Support**: Integrates with Vault, PlayerPoints, and TokenManager for pet purchases.
- **Configurable**: Pet types, prices, messages, and shop layout are configurable through YAML files.

## Commands & Permissions

The main command is `/pets` (or `/pet`).

*   **/pets** - Base command for the plugin.
    *   **Permission**: `petsplus.use` (Allows basic pet commands, default: true)

*   **/pets shop** - Opens the pet shop menu.
    *   **Permission**: `petsplus.shop` (Allows opening the pet shop, default: true)

*   **Administrative Commands**
    *   Requires permission: `petsplus.admin` (default: op)

### Purchase Permissions

*   `petsplus.buy`: Allows a player to purchase any pet. (default: true)
*   `petsplus.buy.<pet_type>`: Allows a player to purchase a specific pet type (e.g., `petsplus.buy.pig`). (default: op)
*   `petsplus.buy.*`: A wildcard permission to allow buying all pets. (default: op)

### Other Permissions

*   `petsplus.discount.<percent>`: Gives a player a percentage discount in the shop (e.g., `petsplus.discount.10` for 10% off). (default: op)

## Configuration

The plugin's behavior can be customized through the files located in the `/plugins/KartaPetsPlus/` directory:

- **`config.yml`**: Main configuration file.
- **`messages.yml`**: Customize all user-facing messages.
- **`pets.yml`**: Defines all the pets available in the shop.
- **`shop.yml`**: Configures the layout and settings of the pet shop.