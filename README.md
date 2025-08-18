# KartaPetsPlus

**Version: 2.0.0**

KartaPetsPlus is a feature-rich pets plugin for Spigot-based servers that provides an immersive experience for players to buy, collect, and summon their own companions.

## Features

- **Advanced GUI Shop**: A clean, intuitive, and fully configurable GUI shop for players to browse and purchase pets.
- **Confirmation on Purchase**: A confirmation screen to prevent accidental purchases.
- **GUI-Based Pet Management**: Players can view their collected pets in a simple GUI.
- **Multiple Currency Support**: Seamlessly integrates with Vault (for economy plugins like EssentialsX), PlayerPoints, and TokenManager.
- **Highly Configurable**: Almost every aspect of the plugin can be customized through easy-to-edit YAML files.
  - Define unlimited pet types with custom names, entities, and prices.
  - Customize the shop layout, item names, and lore.
  - Change every message sent to players.
- **Pet Previews**: Players can right-click a pet in the shop to see a live preview before buying.
- **Asynchronous by Default**: Economy and data storage operations are handled asynchronously to prevent server lag.

## Commands & Permissions

The main command is `/pets` (or `/pet`).

- **`/pets help`**
  - **Description**: Shows the help message.
  - **Permission**: `petsplus.use` (default: `true`)

- **`/pets shop`**
  - **Description**: Opens the pet shop menu.
  - **Permission**: `petsplus.shop` (default: `true`)

- **`/pets list`**
  - **Description**: Opens a GUI showing all pets you have purchased.
  - **Permission**: `petsplus.use` (default: `true`)

- **`/pets summon <pet_type>`**
  - **Description**: Summons one of your purchased pets.
  - **Permission**: `petsplus.use` (default: `true`)

- **`/pets dismiss`**
  - **Description**: Dismisses your currently active pet.
  - **Permission**: `petsplus.use` (default: `true`)

- **`/pets rename <new_name>`**
  - **Description**: Renames your active pet. Supports color codes.
  - **Permission**: `petsplus.use` (default: `true`)

- **`/pets reload`**
  - **Description**: Reloads all configuration files.
  - **Permission**: `petsplus.admin` (default: `op`)

### Purchase Permissions

- `petsplus.buy`: Allows a player to purchase any pet. (default: `true`)
- `petsplus.buy.<pet_type>`: Allows a player to purchase a specific pet type (e.g., `petsplus.buy.wolf`). (default: `op`)
- `petsplus.buy.*`: A wildcard permission to allow buying all pets. (default: `op`)
- `petsplus.discount.<percent>`: Gives a player a percentage discount in the shop (e.g., `petsplus.discount.10` for 10% off). (default: `op`)

## Configuration

The plugin's behavior can be customized through the files located in the `/plugins/KartaPetsPlus/` directory.

### `config.yml`
- Contains general settings like the maximum number of pets, default pet on join, and storage type (YAML, SQLite, MySQL).

### `messages.yml`
- Customize all user-facing messages, including the plugin prefix.

### `pets.yml`
- This is where you define all the pets that can exist on the server. You can add as many as you like.
- Each pet needs an internal name (e.g., `baby_dragon`), a `display-name`, an `entity` type (from Minecraft), and a `price`.

### `shop.yml`
- This file gives you deep control over the appearance and functionality of the `/pets shop` GUI.
- You can change the title, size, and layout of the shop.
- You can override the default settings for any pet, changing its price, icon, or making it unpurchasable.

#### Advanced Lore Configuration
The `shop.yml` file now allows for advanced customization of the item lore in the shop based on whether a player has purchased the pet.

Under the `defaults` section (and in any `overrides` entry), you can now define:
- `lore-locked`: The lore to display for a pet the player has not yet purchased.
- `lore-unlocked`: The lore to display for a pet the player already owns.
- `status-locked`: The text for the `%pet_status%` placeholder when a pet is available for purchase.
- `status-unlocked`: The text for the `%pet_status%` placeholder when a pet is owned.
- `status-unavailable`: The text for the `%pet_status%` placeholder when a pet is not purchasable.

**Example:**
```yaml
defaults:
  name: "&f%pet_display_name% - %pet_status%"
  lore-locked:
    - "&7Price: &6%pet_price% %currency_symbol%"
    - ""
    - "&eClick to purchase!"
  lore-unlocked:
    - "&aYou own this pet!"
    - "&7Right-click to preview."
  status-locked: "&eClick to Buy"
  status-unlocked: "&aOwned"
  status-unavailable: "&cNot for Sale"
```
This system gives you full control over how items are presented to players in the shop.