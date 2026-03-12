package Al3x.starsBarrel.commands;

import Al3x.starsBarrel.StarsBarrel;
import Al3x.starsBarrel.models.KeyType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveKeyCommand implements CommandExecutor {

    private final StarsBarrel plugin;

    public GiveKeyCommand(StarsBarrel plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("starsbarrel.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length < 3 || !args[0].equalsIgnoreCase("give")) {
            return false;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Игрок не найден!");
            return true;
        }

        KeyType keyType = KeyType.fromString(args[2]);
        if (keyType == null) {
            sender.sendMessage(ChatColor.RED + "Неверный тип отмычки! Доступные: common, uncommon, rare, epic, mythic, legendary");
            return true;
        }

        ItemStack key = keyType.createKeyItem(plugin);
        target.getInventory().addItem(key);

        String message = plugin.getConfigManager().getMessage("key-given")
                .replace("%type%", keyType.name())
                .replace("%player%", target.getName());
        sender.sendMessage(message);

        if (target != sender) {
            target.sendMessage(plugin.getConfigManager().getMessage("key-received")
                    .replace("%type%", keyType.name()));
        }

        return true;
    }
}