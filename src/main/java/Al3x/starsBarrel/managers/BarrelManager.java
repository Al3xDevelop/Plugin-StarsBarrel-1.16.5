package Al3x.starsBarrel.managers;

import Al3x.starsBarrel.StarsBarrel;
import Al3x.starsBarrel.models.Barrel;
import Al3x.starsBarrel.models.KeyType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class BarrelManager {

    private final StarsBarrel plugin;
    private final Map<Location, Al3x.starsBarrel.models.Barrel> barrels = new HashMap<>();
    private final Map<KeyType, Double> keyBonuses = new HashMap<>();
    private final Map<KeyType, Integer> guaranteedItems = new HashMap<>();

    public BarrelManager(StarsBarrel plugin) {
        this.plugin = plugin;
        loadKeyBonuses();
        loadGuaranteedItems();
        loadBarrels();
    }

    private void loadKeyBonuses() {
        keyBonuses.put(KeyType.COMMON, plugin.getConfig().getDouble("keys.common.chance", 5.0));
        keyBonuses.put(KeyType.UNCOMMON, plugin.getConfig().getDouble("keys.uncommon.chance", 3.0));
        keyBonuses.put(KeyType.RARE, plugin.getConfig().getDouble("keys.rare.chance", 2.0));
        keyBonuses.put(KeyType.EPIC, plugin.getConfig().getDouble("keys.epic.chance", 1.0));
        keyBonuses.put(KeyType.MYTHIC, plugin.getConfig().getDouble("keys.mythic.chance", 0.5));
        keyBonuses.put(KeyType.LEGENDARY, plugin.getConfig().getDouble("keys.legendary.chance", 0.2));
    }

    private void loadGuaranteedItems() {
        guaranteedItems.put(KeyType.COMMON, plugin.getConfig().getInt("keys.common.guaranteed", 0));
        guaranteedItems.put(KeyType.UNCOMMON, plugin.getConfig().getInt("keys.uncommon.guaranteed", 0));
        guaranteedItems.put(KeyType.RARE, plugin.getConfig().getInt("keys.rare.guaranteed", 1));
        guaranteedItems.put(KeyType.EPIC, plugin.getConfig().getInt("keys.epic.guaranteed", 2));
        guaranteedItems.put(KeyType.MYTHIC, plugin.getConfig().getInt("keys.mythic.guaranteed", 3));
        guaranteedItems.put(KeyType.LEGENDARY, plugin.getConfig().getInt("keys.legendary.guaranteed", 4));
    }

    private void loadBarrels() {
        barrels.clear();
        for (Al3x.starsBarrel.models.Barrel barrel : plugin.getDatabaseManager().loadAllBarrels()) {
            World world = plugin.getServer().getWorld(barrel.getWorld());
            if (world != null) {
                Location loc = new Location(world, barrel.getX(), barrel.getY(), barrel.getZ());
                barrels.put(loc, barrel);
            }
        }
    }

    public boolean setBarrel(Location location) {
        if (barrels.containsKey(location)) {
            return false;
        }

        Block block = location.getBlock();
        if (block.getType() != Material.BARREL) {
            block.setType(Material.BARREL);
        }

        BlockData blockData = block.getBlockData();
        if (blockData instanceof Directional) {
            Directional directional = (Directional) blockData;
            directional.setFacing(BlockFace.UP);
            block.setBlockData((org.bukkit.block.data.BlockData) directional);
        }

        Al3x.starsBarrel.models.Barrel barrel = new Al3x.starsBarrel.models.Barrel(location);
        barrels.put(location, barrel);
        plugin.getDatabaseManager().saveBarrel(barrel);

        return true;
    }

    public void removeBarrel(Location location) {
        barrels.remove(location);
        plugin.getDatabaseManager().removeBarrel(location);
    }

    public boolean isBarrel(Location location) {
        return barrels.containsKey(location);
    }

    public void openBarrelGUI(Player player, KeyType keyType) {
        List<ItemStack> drops = generateDrops(keyType);

        String title = ChatColor.translateAlternateColorCodes('&',
                "&8Бочка с лутом [" + keyType.name() + "]");

        Inventory barrelGUI = Bukkit.createInventory(null, 27, title);

        Random random = new Random();
        List<Integer> availableSlots = new ArrayList<>();
        for (int i = 0; i < 27; i++) {
            availableSlots.add(i);
        }
        Collections.shuffle(availableSlots);

        for (int i = 0; i < drops.size() && i < availableSlots.size(); i++) {
            barrelGUI.setItem(availableSlots.get(i), drops.get(i));
        }

        player.openInventory(barrelGUI);
    }

    private List<ItemStack> generateDrops(KeyType keyType) {
        List<ItemStack> drops = new ArrayList<>();
        List<DatabaseManager.DropItem> allDrops = plugin.getDatabaseManager().getAllDropItems();

        if (allDrops.isEmpty()) {
            return drops;
        }

        Random random = new Random();
        double keyBonus = keyBonuses.getOrDefault(keyType, 0.0);
        int guaranteed = guaranteedItems.getOrDefault(keyType, 0);

        for (int i = 0; i < guaranteed; i++) {
            if (!allDrops.isEmpty()) {
                DatabaseManager.DropItem randomItem = allDrops.get(random.nextInt(allDrops.size()));
                drops.add(randomItem.getItem().clone());
            }
        }

        for (DatabaseManager.DropItem dropItem : allDrops) {
            double baseChance = dropItem.getChance();
            double finalChance = baseChance + keyBonus;
            finalChance = Math.min(100.0, finalChance);

            int maxItems = (int) (finalChance / 10);
            if (maxItems < 1) maxItems = 1;

            for (int attempt = 0; attempt < maxItems; attempt++) {
                if (random.nextDouble() * 100 <= finalChance) {
                    drops.add(dropItem.getItem().clone());
                }
            }
        }

        Collections.shuffle(drops);

        return drops;
    }

    public void reloadBarrels() {
        loadBarrels();
        loadKeyBonuses();
        loadGuaranteedItems();
    }
}