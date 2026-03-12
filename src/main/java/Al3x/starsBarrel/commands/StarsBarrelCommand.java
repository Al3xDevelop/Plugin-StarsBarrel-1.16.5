package Al3x.starsBarrel.commands;

import Al3x.starsBarrel.StarsBarrel;
import Al3x.starsBarrel.models.KeyType;
import Al3x.starsBarrel.utils.MenuBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class StarsBarrelCommand implements CommandExecutor {

    private final StarsBarrel plugin;

    public StarsBarrelCommand(StarsBarrel plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        if (!sender.hasPermission("starsbarrel.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give":
                return giveCommand(sender, args);
            case "createdrop":
                return createDropCommand(sender);
            case "setbarrel":
                return setBarrelCommand(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean giveCommand(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Использование: /starsbarrel give <игрок> <тип>");
            return true;
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

        if (sender != target) {
            target.sendMessage(plugin.getConfigManager().getMessage("key-received")
                    .replace("%type%", keyType.name()));
        }

        return true;
    }

    private boolean createDropCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("only-players"));
            return true;
        }

        Player player = (Player) sender;
        MenuBuilder.openDropMenu(plugin, player);
        return true;
    }

    private boolean setBarrelCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("only-players"));
            return true;
        }

        Player player = (Player) sender;
        Block targetBlock = player.getTargetBlockExact(5);

        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-looking-at-block"));
            return true;
        }

        Location location = targetBlock.getLocation();

        if (plugin.getBarrelManager().isBarrel(location)) {
            plugin.getBarrelManager().removeBarrel(location);
            player.sendMessage(plugin.getConfigManager().getMessage("barrel-remove"));
        } else {
            if (plugin.getBarrelManager().setBarrel(location)) {
                player.sendMessage(plugin.getConfigManager().getMessage("barrel-set"));
            } else {
                player.sendMessage(plugin.getConfigManager().getMessage("already-barrel"));
            }
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "StarsBarrel Commands");
        sender.sendMessage(ChatColor.YELLOW + "/starsbarrel give <игрок> <тип> " + ChatColor.WHITE + "- Выдать отмычку");
        sender.sendMessage(ChatColor.YELLOW + "/starsbarrel createdrop " + ChatColor.WHITE + "- Открыть меню настройки дропа");
        sender.sendMessage(ChatColor.YELLOW + "/starsbarrel setbarrel " + ChatColor.WHITE + "- Установить/удалить бочку");
        sender.sendMessage(ChatColor.GRAY + "Типы отмычек: common, uncommon, rare, epic, mythic, legendary");
    }
}