package com.cerrealic.ezbuy.commands;

import com.cerrealic.cerspilib.Cerspi;
import com.cerrealic.cerspilib.CerspiCommand;
import com.cerrealic.cerspilib.logging.Formatter;
import com.cerrealic.ezbuy.EzBuy;
import com.cerrealic.ezbuy.EzBuyConfig;
import com.cerrealic.ezbuy.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;

import java.util.Arrays;
import java.util.List;

@Commands(
		@org.bukkit.plugin.java.annotation.command.Command(
				name = "ezbuy",
				desc = "General EzBuy command.",
				usage = "/<command> <option>",
				permission = Permissions.COMMAND_EZBUY
		)
)
@org.bukkit.plugin.java.annotation.permission.Permissions(
		{
				@Permission(
						name = Permissions.COMMAND_EZBUY,
						desc = "Allows use of the /ezbuy command.",
						defaultValue = PermissionDefault.OP
				),
				@Permission(
						name = Permissions.COMMAND_EZBUY_DEBUG,
						desc = "Allows toggling debug mode.",
						defaultValue = PermissionDefault.FALSE
				),
				@Permission(
						name = Permissions.COMMAND_EZBUY_PROFITRATE,
						desc = "Allows setting profit rate.",
						defaultValue = PermissionDefault.FALSE
				)
		}
)
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

		Player player = (Player) sender;
		ezBuy.getCerspiLogger().setTarget((Conversable) sender);
		EzBuyConfig config = ezBuy.getEzBuyConfig();

		switch (args[0]) {
			case OPT_DEBUG:
				if (Cerspi.assertPermissions(ezBuy, player, Permissions.COMMAND_EZBUY_DEBUG)) {
					ezBuy.setDebugMode(!ezBuy.getDebugger().isEnabled());
				}
				return true;
			case OPT_PROFIT_RATE:
				if (Cerspi.assertPermissions(ezBuy, player, Permissions.COMMAND_EZBUY_PROFITRATE)) {
					double input;

					try {
						input = Double.parseDouble(args[0]);
					} catch (Exception ex) {
						ezBuy.getCerspiLogger().log(new Formatter("Please give a valid decimal number.").stylizeError().toString(), false);
						return false;
					}

					config.setProfitRate(input);
					ezBuy.getCerspiLogger().log(new Formatter("Profit rate set to " + input).stylizeSuccess().toString(), false);
				}
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
