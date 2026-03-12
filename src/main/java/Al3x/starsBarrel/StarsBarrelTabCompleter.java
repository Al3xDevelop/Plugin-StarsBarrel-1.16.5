package Al3x.starsBarrel;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StarsBarrelTabCompleter implements TabCompleter {

    private static final List<String> COMMANDS = Arrays.asList("give", "createdrop", "setbarrel");
    private static final List<String> KEY_TYPES = Arrays.asList("common", "uncommon", "rare", "epic", "mythic", "legendary");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("starsbarrel.admin")) {
            return completions;
        }

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], COMMANDS, completions);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give")) {
                for (Player player : sender.getServer().getOnlinePlayers()) {
                    if (StringUtil.startsWithIgnoreCase(player.getName(), args[1])) {
                        completions.add(player.getName());
                    }
                }
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("give")) {
                StringUtil.copyPartialMatches(args[2], KEY_TYPES, completions);
            }
        }

        Collections.sort(completions);
        return completions;
    }
}