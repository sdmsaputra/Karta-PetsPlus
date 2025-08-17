# KartaPetsPlus

**Version: 2.2.2-BETA**

KartaPetsPlus is a feature-rich pets plugin for PaperMC servers, designed to provide a seamless and engaging pet ownership experience. It includes a fully functional pet shop, detailed pet management, and extensive customization options.

## What's New in 2.2.2-BETA?

- **Pet Combat Fixes**:
    - Fixed a bug that allowed pets to attack each other.
    - Fixed a bug where pets added to the shop via command would attack their owners.
- **Command Suggestions**: Added tab-completion to all commands, including suggestions for pet IDs, entity types, and item/block icons.
- **Updated Messages**: The `messages.yml` file has been updated to include all messages used throughout the plugin, with clear categorization for easier management.

## What's New in 2.2.1-BETA?

- **Pet Rename Feature**: The pet rename feature has been fixed and is now fully functional.
- **Italics Fix**: Fixed a bug where item names in the GUIs would appear italicized by default.
- **Placeholder Fix**: Placeholders are now consistently using the `{placeholder}` format.
- **New Edit Command**: Added the `/petshop edit` command for easy management of pets in the shop.
- **Improved Add Command**: The `/petshop add` command now supports optional `icon` and `description` arguments.

## Features

- **Pet Ownership**: Players can buy, summon, stow, and rename their pets.
- **GUI Menus**: Easy-to-use GUI menus for managing pets (`/pets`) and browsing the shop (`/petshop`).
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