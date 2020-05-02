package com.cerrealic.ezbuy;

import com.cerrealic.cerspilib.logging.Debug;
import com.cerrealic.cerspilib.logging.Log;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.conversations.Conversable;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class CommandEzBuy implements CommandExecutor, TabCompleter {
	public static final String LABEL = "ezbuy";
	private EzBuy plugin;

	/**
	 * Executes the given command, returning its success.
	 * <br>
	 * If false is returned, then the "usage" plugin.yml entry for this command
	 * (if defined) will be sent to the player.
	 *
	 * @param sender  Source of the command
	 * @param command Command which was executed
	 * @param label   Alias of the command which was used
	 * @param args    Passed command arguments
	 * @return true if a valid command, otherwise false
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Context.lastUser = Log.target = Debug.target = (Conversable) sender;
		plugin = Context.plugin;
		FileConfiguration config = plugin.getConfig();

		switch (args[0]) {
			case "debug":
				if (sender instanceof Player && !plugin.assertPermissions((Player) sender, Permissions.COMMAND_EZBUY_ALL, Permissions.COMMAND_DEBUG)) {
					return true;
				}

				Debug.enabled = !Debug.enabled;
				config.set("debug", Debug.enabled);
				Log.success("Debug " + (Debug.enabled ? "enabled" : "disabled") + ".");
				Context.plugin.saveConfig();
				return true;
			case "update-checking":
				if (sender instanceof Player && !plugin.assertPermissions((Player) sender, Permissions.COMMAND_EZBUY_ALL, Permissions.COMMAND_UPDATE_CHECKING)) {
					return true;
				}

				boolean isCheckingUpdates = config.getBoolean("update-checking", false);
				config.set("update-checking", !isCheckingUpdates);
				Log.success("Update checking " + (!isCheckingUpdates ? "enabled" : "disabled") + ".");
				Context.plugin.saveConfig();
				return true;
			case "cost-increase":
				if (sender instanceof Player && !plugin.assertPermissions((Player) sender, Permissions.COMMAND_EZBUY_ALL, Permissions.COMMAND_COST_INCREASE)) {
					return true;
				}

				try {
					double input = Double.parseDouble(args[0]);
					config.set("cost-increase", input);
					Log.success("Cost increase set to " + input);
					Context.plugin.saveConfig();
					return true;
				} catch (Exception ex) {
					Log.error("Please give a valid decimal number.");
					return false;
				}
		}

		return false;
	}

	/**
	 * Requests a list of possible completions for a command argument.
	 *
	 * @param sender  Source of the command.  For players tab-completing a
	 *                command inside of a command block, this will be the player, not
	 *                the command block.
	 * @param command Command which was executed
	 * @param alias   The alias used
	 * @param args    The arguments passed to the command, including final
	 *                partial argument to be completed and command label
	 * @return A List of possible completions for the final argument, or null
	 * to default to the command executor
	 */
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> argNames = Arrays.asList("debug", "update-checking", "cost-increase");

		// return unfiltered
		if (args[0].isEmpty()) {
			return argNames;
		}

		// filter based on current input
		String[] result = argNames.stream()
				.filter((name) -> name.startsWith(args[0]))
				.toArray(String[]::new);

		return Arrays.asList(result);
	}
}
