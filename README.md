# KartaPetsPlus

**Version: 3.0.1**

KartaPetsPlus is a feature-rich pets plugin for PaperMC servers, designed to provide a seamless and engaging pet ownership experience. It includes a fully functional pet shop, detailed pet management, and extensive customization options.

## What's New in 3.0.1?

- **GUI Overhaul**: Fixed a major bug that made items in the pet shop unclickable and movable. The entire GUI handling system has been refactored to be more robust and reliable, using modern Bukkit API practices.
- **The Entire Zoo**: The pet shop has been expanded to include every single mob from Minecraft 1.21 (excluding bosses). From Allays to Zoglins, you can now own almost any creature as a pet.
- **Standardized Naming**: All pet display names in the default configuration have been standardized to the `MobName Pet` format for consistency (e.g., "Pig Pet", "Creeper Pet").
- **Configuration Cleanup**: The default `pets.yml` has been streamlined, removing unnecessary lore and `head-texture` values to simplify configuration and improve performance.

## Features

- **Pet Ownership**: Players can buy, summon, stow, and rename their pets.
- **GUI Menus**: Easy-to-use GUI menus for managing pets (`/pets`) and browsing the shop (`/petshop`).
- **All Mobs as Pets**: The default configuration now includes every non-boss mob from Minecraft 1.21.
- **Economy Support**: Integrates with Vault and PlayerPoints for purchasing pets.
- **Configurable**: Almost everything is configurable, from pet types and prices to messages and menu layouts.
- **PlaceholderAPI Support**: A wide range of placeholders to display pet information.
- **MySQL Storage**: Support for storing player data in a MySQL database.
- **Command Suggestions**: Full tab-completion support for all commands and their arguments.

## Commands & Permissions

*   **/pets** - Opens the pet management menu.
    *   **Permission**: `kartapetsplus.use`
*   **/petshop** - Opens the pet shop menu.
    *   **Permission**: `kartapetsplus.shop`
*   **/petshop add <entityType> <price> [icon] [description]** - Adds a new pet to the shop.
    *   **Permission**: `kartapetsplus.admin`
*   **/petshop edit <petId> <name|icon|description|price|delete> [value]** - Edits a pet in the shop.
    *   **Permission**: `kartapetsplus.admin`
*   **/petsreload** - Reloads the plugin configuration.
    *   **Permission**: `kartapetsplus.admin`

### Additional Permissions
*   `kartapetsplus.limit.<amount>` - Sets the maximum number of pets a player can own.

## Placeholders

KartaPetsPlus provides the following placeholders through PlaceholderAPI:

*   `%kartapetsplus_pet_count%` - The total number of pets a player owns.
*   `%kartapetsplus_has_active_pet%` - Returns 'yes' if the player has a pet summoned, otherwise 'no'.
*   `%kartapetsplus_active_pet_name%` - The name of the currently summoned pet.
*   `%kartapetsplus_active_pet_type%` - The display name of the currently summoned pet's type.
*   `%kartapetsplus_active_pet_status%` - The status of the currently summoned pet (e.g., `SUMMONED`).

## Configuration

The plugin's behavior can be customized through four main files located in the `/plugins/KartaPetsPlus/` directory:

- **`config.yml`**: Main configuration file. Used to set the storage type (YAML or MySQL), database credentials, economy provider, and default pet limits.
- **`pets.yml`**: Defines all the pets available in the shop. You can set their name, icon, price, and lore here.
- **`messages.yml`**: Customize all user-facing messages, such as command responses and notifications.
- **`gui.yml`**: Customize the GUIs, including titles, sizes, and fill items.

All configuration files and messages support the [MiniMessage](https://docs.advntr.dev/minimessage/format.html) format for advanced text styling.