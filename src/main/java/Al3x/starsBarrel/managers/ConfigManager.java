package Al3x.starsBarrel.managers;

import Al3x.starsBarrel.StarsBarrel;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final StarsBarrel plugin;

    public ConfigManager(StarsBarrel plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
    }

    public String getMessage(String key) {
        String message = plugin.getConfig().getString("messages." + key, "&cMessage not found: " + key);
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', message);
    }
}