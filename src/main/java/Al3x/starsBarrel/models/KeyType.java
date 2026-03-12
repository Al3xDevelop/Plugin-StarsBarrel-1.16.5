package Al3x.starsBarrel.models;

import Al3x.starsBarrel.StarsBarrel;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public enum KeyType {
    COMMON("common", 5.0),
    UNCOMMON("uncommon", 3.0),
    RARE("rare", 2.0),
    EPIC("epic", 1.0),
    MYTHIC("mythic", 0.5),
    LEGENDARY("legendary", 0.2);

    private final String configKey;
    private final double defaultChance;

    KeyType(String configKey, double defaultChance) {
        this.configKey = configKey;
        this.defaultChance = defaultChance;
    }

    public String getConfigKey() {
        return configKey;
    }

    public double getDefaultChance() {
        return defaultChance;
    }

    public static KeyType fromString(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public ItemStack createKeyItem(StarsBarrel plugin) {
        String path = "keys." + configKey + ".";
        Material material = Material.getMaterial(plugin.getConfig().getString(path + "material", "TRIPWIRE_HOOK"));
        if (material == null) material = Material.TRIPWIRE_HOOK;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String name = plugin.getConfig().getString(path + "name", "&fKey");
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        List<String> lore = new ArrayList<>();
        for (String line : plugin.getConfig().getStringList(path + "lore")) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(lore);

        if (plugin.getConfig().getBoolean(path + "enchanted", false)) {
            meta.addEnchant(Enchantment.LUCK, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(meta);

        return item;
    }

    public double getChance(StarsBarrel plugin) {
        return plugin.getConfig().getDouble("keys." + configKey + ".chance", defaultChance);
    }

    public int getGuaranteed(StarsBarrel plugin) {
        return plugin.getConfig().getInt("keys." + configKey + ".guaranteed", 0);
    }
}