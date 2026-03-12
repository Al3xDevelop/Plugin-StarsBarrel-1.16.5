package Al3x.starsBarrel.commands;

import Al3x.starsBarrel.StarsBarrel;
import Al3x.starsBarrel.utils.MenuBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateDropCommand implements CommandExecutor {

    private final StarsBarrel plugin;

    public CreateDropCommand(StarsBarrel plugin) {
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

        if (args.length < 1 || !args[0].equalsIgnoreCase("createdrop")) {
            return false;
        }

        Player player = (Player) sender;
        MenuBuilder.openDropMenu(plugin, player);

        return true;
    }
}