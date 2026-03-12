package Al3x.starsBarrel.listeners;

import Al3x.starsBarrel.StarsBarrel;
import Al3x.starsBarrel.models.KeyType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class BarrelListener implements Listener {

    private final StarsBarrel plugin;

    public BarrelListener(StarsBarrel plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBarrelInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.BARREL) {
            return;
        }

        Location location = block.getLocation();
        if (!plugin.getBarrelManager().isBarrel(location)) {
            return;
        }

        event.setCancelled(true);

        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        ItemStack item = inventory.getItemInMainHand();

        KeyType keyType = getKeyType(item);
        if (keyType == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("need-key"));
            return;
        }

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            inventory.setItemInMainHand(null);
        }

        plugin.getBarrelManager().openBarrelGUI(player, keyType);
    }

    private KeyType getKeyType(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return null;
        }

        for (KeyType type : KeyType.values()) {
            ItemStack keyItem = type.createKeyItem(plugin);
            if (keyItem.isSimilar(item)) {
                return type;
            }
        }

        return null;
    }
}