package com.cerrealic.ezbuy.commands;

import com.cerrealic.cerspilib.CerspiCommand;
import com.cerrealic.cerspilib.logging.Debug;
import com.cerrealic.cerspilib.logging.Log;
import com.cerrealic.ezbuy.EzBuy;
import com.cerrealic.ezbuy.EzBuyConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversable;

import java.util.Arrays;
import java.util.List;

public class CommandEzBuy extends CerspiCommand {
	public static final String LABEL = "ezbuy";
	private static final String OPT_DEBUG = "debug";
	private static final String OPT_PROFIT_RATE = "profitRate";
	private EzBuy ezBuy;

	public CommandEzBuy(EzBuy ezBuy) {
		this.ezBuy = ezBuy;
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

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
		if (args.length <= 0) {
			return false;
		}

		if (!(sender instanceof Conversable)) {
			return true;
		}

		Log.target = Debug.target = (Conversable) sender;
		EzBuyConfig config = ezBuy.getEzBuyConfig();

		switch (args[0]) {
			case OPT_DEBUG:
				Debug.enabled = !Debug.enabled;
				config.setDebugMode(Debug.enabled);
				Log.success("Debug " + (Debug.enabled ? "enabled" : "disabled") + ".");
				return true;
//			case "update-checking":
//				if (sender instanceof Player && !Cerspi.assertPermission((Player) sender, Permissions.COMMAND_EZBUY_ALL, Permissions.COMMAND_UPDATE_CHECKING)) {
//					return true;
//				}
//
//				boolean isCheckingUpdates = config.getBoolean("update-checking", false);
//				config.set("update-checking", !isCheckingUpdates);
//				Log.success("Update checking " + (!isCheckingUpdates ? "enabled" : "disabled") + ".");
//				plugin.saveConfig();
//				return true;
			case OPT_PROFIT_RATE:
				double input;

				try {
					input = Double.parseDouble(args[0]);
				} catch (Exception ex) {
					Log.error("Please give a valid decimal number.");
					return false;
				}

				config.setProfitRate(input);
				Log.success("Profit rate set to " + input);
				return true;
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
		List<String> argNames = Arrays.asList(OPT_DEBUG, OPT_PROFIT_RATE);

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
