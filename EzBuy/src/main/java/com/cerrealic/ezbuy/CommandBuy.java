package com.cerrealic.ezbuy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandBuy implements CommandExecutor {

	private EzBuy plugin;

	public CommandBuy(EzBuy plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("The /buy command is only available to players.");
			return true;
		}

		if (args.length == 0 || args.length > 2) {
			return false;
		}

		Player player = (Player) sender;
		Economy economy = plugin.getEconomy();

		// Lets give the player 1.05 currency (note that SOME economic plugins require rounding!)
		sender.sendMessage(String.format("You have %s",
				economy.format(economy.getBalance(Bukkit.getOfflinePlayer(player.getUniqueId())))));
		EconomyResponse r = economy.depositPlayer(player, 1.05);
		if(r.transactionSuccess()) {
			sender.sendMessage(String.format("You were given %s and now have %s", economy.format(r.amount), economy.format(r.balance)));
		} else {
			sender.sendMessage(String.format("An error occurred: %s", r.errorMessage));
		}

		/*
		int amount = 1;

		try {
			amount = Integer.parseInt(args[1]);
		} catch (NumberFormatException ex) {
			sender.sendMessage("Buying failed: The amount you entered was not a valid number.");
			return false;
		} catch (Exception e) {
			sender.sendMessage("Buying failed: There was an unexpected error with the amount you entered. Please ask a server admin for help.");
			return false;
		}

		if (amount <= 0) {
			return true;
		}

		ItemStack diamonds = new ItemStack(Material.DIAMOND, amount);
		player.getInventory().addItem(diamonds);
		*/
		return true;
	}

}
