# KartaPetsPlus - Pet Plugin for PaperMC

KartaPetsPlus is a feature-rich pets plugin for PaperMC servers, designed to provide a comprehensive and enjoyable pet system for players.

**Version:** `1.0.1-SNAPSHOT`

## Features

- **Pet Shop:** A fully-featured GUI shop where players can purchase pets.
- **Economy Support:** Integrates with Vault for economy support.
- **Customizable Pets:** Define your own pets with custom names, prices, and appearances in `pets.yml`.
- **Player Data Storage:** Securely stores player pet data in individual YAML files.
- **PlaceholderAPI Support:** Provides placeholders to display pet information in other plugins.
- **Modern API:** Built with the modern Paper API, including Adventure components for all messaging.

## Dependencies

- **[Vault](https://www.spigotmc.org/resources/vault.34315/)** (Required): For handling economy transactions.
- **[PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)** (Optional): For using placeholders in other plugins.

## Commands & Permissions

| Command        | Description                        | Permission              |
|----------------|------------------------------------|-------------------------|
| `/pets`        | Opens the pet management menu.     | `kartapetsplus.use`     |
| `/petshop`     | Opens the pet shop menu.           | `kartapetsplus.shop`    |
| `/petsreload`  | Reloads the plugin configuration.  | `kartapetsplus.admin`   |

## Placeholders

If you have [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) installed, you can use the following placeholders:

- `%kartapetsplus_pet_count%`: Displays the total number of pets a player owns.