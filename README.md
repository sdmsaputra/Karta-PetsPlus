# KartaPetsPlus

**Version: 2.2.0**

KartaPetsPlus is a feature-rich pets plugin for PaperMC servers, designed to provide a seamless and engaging pet ownership experience. It includes a fully functional pet shop, detailed pet management, and extensive customization options.

## What's New in 2.2.0?

- **Italics Fix**: Fixed a bug where item lore in the pet shop would appear italicized by default.
- **GUI Protection**: The purchase confirmation GUI is now properly protected, preventing players from moving items around.
- **Pet Shop Command**: Implemented the `/petshop add` command, allowing admins to add new pets to the shop without manual configuration. This includes robust validation to prevent errors.

## Features

- **Pet Ownership**: Players can buy, summon, stow, and rename their pets.
- **GUI Menus**: Easy-to-use GUI menus for managing pets (`/pets`) and browsing the shop (`/petshop`).
- **Economy Support**: Integrates with Vault and PlayerPoints for purchasing pets.
- **Configurable**: Almost everything is configurable, from pet types and prices to messages and menu layouts.
- **PlaceholderAPI Support**: A wide range of placeholders to display pet information.
- **MySQL Storage**: Support for storing player data in a MySQL database.

## Commands & Permissions

*   **/pets** - Opens the pet management menu.
    *   **Permission**: `kartapetsplus.use`
*   **/petshop** - Opens the pet shop menu.
    *   **Permission**: `kartapetsplus.shop`
*   **/petshop add <entityType> <price>** - Adds a new pet to the shop.
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

*Note: The older `pet_name`, `pet_type`, and `pet_status` placeholders are still available for backward compatibility.*

## Configuration

The plugin's behavior can be customized through four main files located in the `/plugins/KartaPetsPlus/` directory:

- **`config.yml`**: Main configuration file. Used to set the storage type (YAML or MySQL), database credentials, economy provider, and default pet limits.
- **`pets.yml`**: Defines all the pets available in the shop. You can set their name, icon, price, and lore here.
- **`messages.yml`**: Customize all user-facing messages, such as command responses and notifications.
- **`gui.yml`**: Customize the GUIs, including titles, sizes, and fill items. You can also define the exact slots for items in the pet shop and pet menu.
    - **`pet-shop.items`**: A list of pets to be displayed in the shop. Each entry is a pet ID from `pets.yml` and has a `slot` number.
    - **`pet-menu.slots`**: A list of inventory slots where players' owned pets will be displayed.

All configuration files and messages support the [MiniMessage](https://docs.advntr.dev/minimessage/format.html) format for advanced text styling.