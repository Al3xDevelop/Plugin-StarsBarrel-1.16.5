package Al3x.starsBarrel.utils;

import Al3x.starsBarrel.StarsBarrel;
import Al3x.starsBarrel.managers.DatabaseManager;
import Al3x.starsBarrel.listeners.DropMenuListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MenuBuilder {

    public static void openDropMenu(StarsBarrel plugin, Player player) {
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.menu-title", "&8Настройка дропа"));

        Inventory inventory = Bukkit.createInventory(null, 54, title);

        List<DatabaseManager.DropItem> dropItems = plugin.getDatabaseManager().getAllDropItems();

        plugin.getLogger().info("Загрузка " + dropItems.size() + " предметов в меню");

        for (DatabaseManager.DropItem dropItem : dropItems) {
            ItemStack item = dropItem.getItem().clone();
            ItemMeta meta = item.getItemMeta();

            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            if (lore == null) lore = new ArrayList<>();

            lore.add(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.chance-format", "&7Шанс: &e%chance%%")
                            .replace("%chance%", String.valueOf(dropItem.getChance()))));

            meta.setLore(lore);
            item.setItemMeta(meta);

            inventory.addItem(item);
        }

        DropMenuListener listener = plugin.getDropMenuListener();
        if (listener != null) {
            listener.addOpenMenu(player, inventory);
        }

        player.openInventory(inventory);
    }
}