package com.cerrealic.ezbuy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
			sender.sendMessage("&cThe /buy command is only available to players.");
			return true;
		}

		if (args.length == 0 || args.length > 2) {
			return false;
		}

		OfflinePlayer player = Bukkit.getOfflinePlayer(((Player) sender).getUniqueId());
		Economy economy = plugin.getEconomy();

		sender.sendMessage(String.format("You have %s",	economy.format(economy.getBalance(player))));

		int amount = 1;

		try {
			amount = Integer.parseInt(args[1]);
		} catch (NumberFormatException ex) {
			sender.sendMessage("&cBuying failed: The amount you entered was not a valid number.");
			return false;
		} catch (Exception e) {
			sender.sendMessage("&cBuying failed: There was an unexpected error with the amount you"
					+ " entered. Please ask a server admin for help.");
			return false;
		}

		if (amount <= 0) {
			return true;
		}

		EconomyResponse r = economy.withdrawPlayer(player, amount);

		if(r.transactionSuccess()) {
			sender.sendMessage(String.format("You were given %s and now have %s", economy.format(r.amount), economy.format(r.balance)));
		} else {
			sender.sendMessage(String.format("&cAn error occurred: %s", r.errorMessage));
		}

		/*
		ItemStack diamonds = new ItemStack(Material.DIAMOND, amount);
		player.getInventory().addItem(diamonds);
		*/
		return true;
	}

}
