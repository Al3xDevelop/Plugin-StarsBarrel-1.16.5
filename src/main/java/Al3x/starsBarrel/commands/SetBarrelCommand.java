package Al3x.starsBarrel.commands;

import Al3x.starsBarrel.StarsBarrel;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetBarrelCommand implements CommandExecutor {

    private final StarsBarrel plugin;

    public SetBarrelCommand(StarsBarrel plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("only-players"));
            return true;
        }

        if (!sender.hasPermission("starsbarrel.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length < 1 || !args[0].equalsIgnoreCase("setbarrel")) {
            return false;
        }

        Player player = (Player) sender;
        Block targetBlock = player.getTargetBlockExact(5);

        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-looking-at-block"));
            return true;
        }

        Location location = targetBlock.getLocation();

        if (plugin.getBarrelManager().isBarrel(location)) {
            // Remove barrel if already exists
            plugin.getBarrelManager().removeBarrel(location);
            player.sendMessage(plugin.getConfigManager().getMessage("barrel-remove"));
        } else {
            // Set new barrel
            if (plugin.getBarrelManager().setBarrel(location)) {
                player.sendMessage(plugin.getConfigManager().getMessage("barrel-set"));
            } else {
                player.sendMessage(plugin.getConfigManager().getMessage("already-barrel"));
            }
        }

        return true;
    }
}