package ch.framedev.blackMarket;

import ch.framedev.csvutils.CsvUtils;
import ch.framedev.simplejavautils.SimpleJavaUtils;
import com.opencsv.exceptions.CsvException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Level;

import static org.bukkit.Bukkit.getScheduler;

public final class BlackMarket extends JavaPlugin implements Listener {

    public Map<Integer, DataMaterial> materialsWithValues;
    public SimpleJavaUtils utils = new SimpleJavaUtils();
    public Map<String, String> searches = new HashMap<>();
    private File materialsFile;
    private VaultManager vaultManager;


    @Override
    public void onEnable() {
        getLogger().log(Level.INFO, "Please wait while Vault is initialized");
        getScheduler().runTaskLaterAsynchronously(this, () -> {
            if (getServer().getPluginManager().getPlugin("Vault") != null) {
                vaultManager = new VaultManager();
                getLogger().log(Level.INFO, "Vault Enabled!");
            } else {
                Bukkit.getConsoleSender().sendMessage("§cYou should have Vault installed!");
            }
        }, 20 * 2);
        materialsFile = new File(getDataFolder(), "materials.csv");
        if (!materialsFile.getParentFile().exists())
            if(!materialsFile.getParentFile().mkdirs())
                getLogger().log(Level.SEVERE, "Cannot create Parent Folder!");

        materialsWithValues = new HashMap<>();
        loadMap();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "Plugin Disabling...");
        materialsWithValues = null;
        vaultManager = null;
        utils = null;
        getLogger().log(Level.INFO, "Plugin Disabled!");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("blackmarket") && sender instanceof Player) {
            if (args.length == 0) {
                searches.remove(sender.getName());
                ((Player) sender).openInventory(createGui(0, ""));
            } else {
                String searchQuery = String.join(" ", args);
                ((Player) sender).openInventory(createGui(0, searchQuery));
                if (!searchQuery.equalsIgnoreCase(""))
                    searches.put(sender.getName(), searchQuery);
            }
            return true;

        }
        return false;
    }


    public void loadMap() {
        String[] rows = {"Id", "Item", "Value", "SellValue"};
        // Load CSV data into materialsWithValues map
        CsvUtils csvUtils = new CsvUtils();
        try {
            if (!materialsFile.exists()) {
                Files.copy(utils.getFromResourceFile("materials.csv", BlackMarket.class).toPath(),
                        materialsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                getLogger().log(Level.INFO, "materials.csv Copied to plugins/BlackMarket/materials.csv");
                List<String[]> list = csvUtils.getDataFromCSVFile(materialsFile, rows);
                list.remove(rows);
                List<String[]> updated = new ArrayList<>();
                for (String[] data : list) {
                    if (data.length < 4) {
                        getLogger().log(Level.WARNING, "Skipping incomplete row: " + Arrays.toString(data));
                        continue; // Skip rows that don't have enough columns
                    }

                    Material material = Material.getMaterial(data[1]);
                    if (material != null && material.isItem()) {
                        updated.add(data);
                    }
                }
                System.out.println("Updated");
                csvUtils.writeCsvFile(materialsFile, Collections.singletonList(rows), updated);
                Bukkit.getConsoleSender().sendMessage("§aYou should consider to update the File (plugins/BlackMarket/materials.csv)!");
            }
            List<String[]> list = csvUtils.getDataFromCSVFile(materialsFile, Collections.singletonList(rows));
            list.remove(rows);
            for (String[] data : list) {
                if (data.length < 4) {
                    getLogger().log(Level.WARNING, "Skipping incomplete row: " + Arrays.toString(data));
                    continue; // Skip rows that don't have enough columns
                }
                Material material = Material.getMaterial(data[1]);
                if(material == null) {
                    getLogger().log(Level.SEVERE, "Invalid material: " + data[1]);
                    continue; // Skip rows that don't have a valid material
                }
                if (material.isItem()) {
                    if (!material.name().contains("SPAWN_EGG"))
                        materialsWithValues.put(Integer.parseInt(data[0]),
                                new DataMaterial(Integer.parseInt(data[0]), data[1],
                                        Double.parseDouble(data[2].replace("_", "")),
                                        Double.parseDouble(data[3].replace("_", ""))));
                }
            }

        } catch (IOException | CsvException e) {
            throw new RuntimeException(e);
        }
    }

    public Inventory createGui(int page, String searchQuery) {
        Inventory gui = Bukkit.createInventory(null, 54, "Black Market - Page " + (page + 1));

        // Filter, sort, and search the materials
        List<DataMaterial> filteredSortedData = materialsWithValues.values().stream()
                .filter(dataMaterial -> Material.getMaterial(dataMaterial.material) != null)
                .filter(dataMaterial -> searchQuery.isEmpty() || dataMaterial.material.toLowerCase().contains(searchQuery.toLowerCase()))
                .sorted(Comparator.comparingInt(dataMaterial -> dataMaterial.id))
                .toList();

        // 5 rows for items, 1 row for navigation
        final int ITEMS_PER_PAGE = 45;
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, filteredSortedData.size());

        for (int i = startIndex; i < endIndex; i++) {
            DataMaterial dataMaterial = filteredSortedData.get(i);
            Material material = Material.getMaterial(dataMaterial.material);
            if (material != null) {
                gui.setItem(i - startIndex, createGuiItem(material, dataMaterial.value, dataMaterial.sellValue));
            }
        }

        // Navigation items
        if (page > 0) {
            gui.setItem(45, createGuiItem(Material.ARROW, "Previous Page"));
        }
        if (endIndex < filteredSortedData.size()) {
            gui.setItem(53, createGuiItem(Material.ARROW, "Next Page"));
        }

        gui.setItem(49, createGuiItem(Material.BARRIER, "Back"));

        return gui;
    }


    private ItemStack createGuiItem(Material material, double value, double sellValue) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(material.name());
            meta.setLore(List.of("Left Click Buy one for " + value,
                    "Right Click Buy a stack for " + value * 64,
                    "Shift Left to Sell one for " + sellValue,
                    "Shift Right to Sell a stack for " + sellValue * 64));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createGuiItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    private void handleAddMaterialsClick(InventoryClickEvent event) {

        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        if (title.contains("Black Market")) {
            event.setCancelled(true);
            String materialName = event.getCurrentItem().getItemMeta().getDisplayName();
            ItemStack item = event.getCurrentItem();

            int page = getPageFromTitle(title);
            String searchQuery;
            // Extract the search query if needed (e.g., from title or another mechanism)
            searchQuery = searches.getOrDefault(player.getName(), "");

            switch (materialName) {
                case "Back":
                    player.closeInventory();
                    break;
                case "Previous Page":
                    player.openInventory(createGui(page - 1, searchQuery));
                    break;
                case "Next Page":
                    player.openInventory(createGui(page + 1, searchQuery));
                    break;
                default:
                    try {
                        Material material = Material.getMaterial(materialName);

                        if (material == null) {
                            player.sendMessage("Invalid material.");
                            return;
                        }

                        int amount = 1;
                        if (event.getClick() == ClickType.RIGHT || event.getClick() == ClickType.SHIFT_RIGHT) {
                            amount = 64;
                        }

                        if (!event.isShiftClick()) {
                            // Buy logic
                            try {
                                double value = Double.parseDouble(item.getItemMeta().getLore().get(0).replace("Left Click Buy one for ", ""));
                                ItemStack toBuy = new ItemStack(material, amount);
                                if (player.getInventory().firstEmpty() == -1) {
                                    player.sendMessage("Not enough space in your inventory.");
                                    return;
                                }
                                if (vaultManager.getEconomy().has(player, amount * value)) {
                                    vaultManager.getEconomy().withdrawPlayer(player, amount * value);
                                    player.sendMessage("Bought " + amount + " " + materialName + " for " + amount * value);
                                    player.getInventory().addItem(toBuy);
                                } else {
                                    player.sendMessage("You don't have enough money to buy " + amount + " " + materialName);
                                }
                            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                                player.sendMessage("Error processing the buy operation.");
                            }
                        } else {
                            // Sell logic
                            try {
                                double sellValue = Double.parseDouble(item.getItemMeta().getLore().get(2).replace("Shift Left to Sell one for ", ""));
                                ItemStack toSell = new ItemStack(material, amount);
                                if (!player.getInventory().containsAtLeast(toSell, amount)) {
                                    player.sendMessage("You don't have enough " + materialName + " to sell.");
                                    return;
                                }
                                player.getInventory().removeItem(toSell);
                                vaultManager.getEconomy().depositPlayer(player, amount * sellValue);
                                player.sendMessage("Sold " + amount + " " + materialName + " for " + amount * sellValue);
                            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                                player.sendMessage("Error processing the sell operation.");
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        player.sendMessage("Invalid material.");
                        event.setCancelled(false);
                    }
            }
        } else {
            event.setCancelled(false);
        }
    }

    private int getPageFromTitle(String title) {
        String[] parts = title.split(" ");
        try {
            return Integer.parseInt(parts[parts.length - 1]) - 1;
        } catch (NumberFormatException e) {
            return 0; // Default to page 0 if parsing fails
        }
    }

    public static <K extends Comparable<? super K>, V> Map<K, V> sortByValue(Map<K, V> map) {
        // Step 1: Convert Map to List of Map entries
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());

        // Step 2: Sort the list with a custom comparator
        list.sort(Map.Entry.comparingByKey());

        // Step 3: Create a new LinkedHashMap and put sorted entries into it
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
