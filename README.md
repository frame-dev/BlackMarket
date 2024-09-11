
# BlackMarket Plugin README

## Overview
The `BlackMarket` plugin is a custom Bukkit plugin that allows players to buy and sell items through a virtual market using Vault's economy system. This plugin features a simple GUI to browse items, filter based on a search query, and manage transactions. Players can purchase or sell items in quantities of one or a stack at a time. The plugin also automatically loads item prices from a CSV file.

## Features
- **Buy and sell items**: Players can buy or sell individual items or stacks from a virtual market using an in-game economy.
- **Custom GUI**: The plugin provides an interactive inventory-based GUI for easy browsing and trading of materials.
- **Search Functionality**: Players can search for specific items in the market.
- **CSV-based item configuration**: The plugin loads item prices from a `materials.csv` file, which can be updated manually.
- **Pagination in the GUI**: The market supports multiple pages, with navigation controls to move between pages.

## Setup
1. **Dependencies**: This plugin requires Vault to be installed and working with a compatible economy plugin.
2. **Installation**:
    - Download and place the plugin JAR in the `plugins` directory.
    - Make sure Vault is installed and configured.
    - Start the server and the plugin will automatically create a `materials.csv` file in `plugins/BlackMarket/` if one doesn’t already exist.
    - Update the `materials.csv` file with item data as needed.

## Commands
- **/blackmarket**: Opens the Black Market GUI.
    - `/blackmarket [search term]`: Opens the market GUI filtered by the search term.

## How It Works

### Vault Integration
- The plugin uses Vault’s economy API to check the player’s balance and handle transactions (buying or selling).

## CSV Format
The `materials.csv` file should be structured as follows:

| Id  | Item         | Value  | SellValue |
| --- | ------------ | ------ | --------- |
| 1   | DIAMOND      | 100.0  | 50.0      |
| 2   | GOLD_INGOT   | 50.0   | 25.0      |

- **Id**: Unique identifier for the item.
- **Item**: Material name (must be a valid Bukkit `Material` enum).
- **Value**: Price to buy one unit of the item.
- **SellValue**: Price to sell one unit of the item.

## License
This plugin is licensed under MIT.
