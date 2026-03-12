package Al3x.starsBarrel.listeners;

import Al3x.starsBarrel.StarsBarrel;
import Al3x.starsBarrel.managers.DatabaseManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class DropMenuListener implements Listener {

    private final StarsBarrel plugin;
    private final Map<UUID, Inventory> openMenus = new HashMap<>();

    public DropMenuListener(StarsBarrel plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        if (!openMenus.containsKey(player.getUniqueId()) ||
                !openMenus.get(player.getUniqueId()).equals(inventory)) {
            return;
        }

        event.setCancelled(false);

        if (event.getClickedInventory() != null && event.getClickedInventory().equals(inventory)) {
            ItemStack currentItem = event.getCurrentItem();
            if (currentItem == null || currentItem.getType() == Material.AIR) return;

            if ((event.isLeftClick() || event.isRightClick()) && !event.isShiftClick()) {
                event.setCancelled(true);

                double currentChance = getChanceFromItem(currentItem);
                double newChance = currentChance;

                if (event.isLeftClick()) {
                    newChance = Math.max(0.1, currentChance - 1);
                } else if (event.isRightClick()) {
                    newChance = Math.min(100, currentChance + 1);
                }

                if (newChance != currentChance) {
                    updateItemChance(currentItem, newChance);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        if (!openMenus.containsKey(player.getUniqueId()) ||
                !openMenus.get(player.getUniqueId()).equals(inventory)) {
            return;
        }

        event.setCancelled(false);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();

        if (!openMenus.containsKey(player.getUniqueId()) ||
                !openMenus.get(player.getUniqueId()).equals(inventory)) {
            return;
        }

        saveAllItems(inventory);
        openMenus.remove(player.getUniqueId());

        player.sendMessage(ChatColor.GREEN + "Дроп сохранен! Всего предметов: " + countItems(inventory));
    }

    private double getChanceFromItem(ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) return 5.0;

        List<String> lore = item.getItemMeta().getLore();
        if (lore == null) return 5.0;

        for (String line : lore) {
            if (line.contains("Шанс:")) {
                try {
                    String chanceStr = ChatColor.stripColor(line).replaceAll("[^0-9.]", "");
                    return Double.parseDouble(chanceStr);
                } catch (NumberFormatException ignored) {}
            }
        }
        return 5.0;
    }

    private void updateItemChance(ItemStack item, double newChance) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        if (lore == null) lore = new ArrayList<>();

        lore.removeIf(line -> line.contains("Шанс:"));

        // Добавляем новую
        lore.add(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.chance-format", "&7Шанс: &e%chance%%")
                        .replace("%chance%", String.valueOf(newChance))));

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private void saveAllItems(Inventory inventory) {
        plugin.getDatabaseManager().deleteAllDropItems();

        int saved = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                double chance = getChanceFromItem(item);

                // Убираем строку с шансом для сохранения
                ItemStack cleanItem = item.clone();
                ItemMeta meta = cleanItem.getItemMeta();
                List<String> lore = meta.getLore();
                if (lore != null) {
                    lore.removeIf(line -> line.contains("Шанс:"));
                    if (lore.isEmpty()) {
                        meta.setLore(null);
                    } else {
                        meta.setLore(lore);
                    }
                    cleanItem.setItemMeta(meta);
                }

                plugin.getDatabaseManager().saveDropItem(cleanItem, chance);
                saved++;
            }
        }
        plugin.getLogger().info("Сохранено " + saved + " предметов в БД");
    }

    private int countItems(Inventory inventory) {
        int count = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                count++;
            }
        }
        return count;
    }

    public void addOpenMenu(Player player, Inventory inventory) {
        openMenus.put(player.getUniqueId(), inventory);
    }
}