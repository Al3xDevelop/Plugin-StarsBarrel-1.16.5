package Al3x.starsBarrel;

import Al3x.starsBarrel.commands.StarsBarrelCommand;
import Al3x.starsBarrel.listeners.BarrelListener;
import Al3x.starsBarrel.listeners.DropMenuListener;
import Al3x.starsBarrel.managers.BarrelManager;
import Al3x.starsBarrel.managers.ConfigManager;
import Al3x.starsBarrel.managers.DatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;

public class StarsBarrel extends JavaPlugin {

    private static StarsBarrel instance;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private BarrelManager barrelManager;
    private DropMenuListener dropMenuListener;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        configManager = new ConfigManager(this);
        databaseManager = new DatabaseManager(this);
        barrelManager = new BarrelManager(this);

        getCommand("starsbarrel").setExecutor(new StarsBarrelCommand(this));

        getCommand("starsbarrel").setTabCompleter(new StarsBarrelTabCompleter());

        getServer().getPluginManager().registerEvents(new BarrelListener(this), this);
        dropMenuListener = new DropMenuListener(this);
        getServer().getPluginManager().registerEvents(dropMenuListener, this);

        getLogger().info("StarsBarrel has been enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
        getLogger().info("StarsBarrel has been disabled!");
    }

    public static StarsBarrel getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public BarrelManager getBarrelManager() {
        return barrelManager;
    }

    public DropMenuListener getDropMenuListener() {
        return dropMenuListener;
    }
}