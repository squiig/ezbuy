package com.cerrealic.ezbuy;

import com.earth2me.essentials.IEssentials;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandBuy implements CommandExecutor, TabCompleter {
	private EzBuy plugin;
	private Economy economy;
	private IEssentials essentials;
	private String label;
	private double buyPriceIncrease;
	private Player player;

	public CommandBuy(EzBuy plugin) {
		this.plugin = plugin;
		label = "buy";
		economy = plugin.getEconomy();
		essentials = plugin.getEssentials();

		buyPriceIncrease = plugin.getConfig().getDouble("cost-increase");
	}

	private void alertCost(Player player, String itemName, double cost) {
		player.sendMessage(String.format("One (1x) of %s currently costs %s", itemName,
				economy.format(cost)));
	}

	private void fail(String message, Object... formatArgs) {
		player.sendMessage(String.format("§cPurchase failed: %s§a", message, formatArgs));
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// Ensure the sender is a player and not a console or command block
		if (!(sender instanceof Player)) {
			sender.sendMessage("§cThe /buy command is only available to players!§a");
			return true;
		}

		// Ensure correct arg amount
		if (args.length == 0 || args.length > 2) {
			return false;
		}

		player = (Player) sender;

		// Figure out what item was meant with the argument
		Material item = Material.matchMaterial(args[0]);

		// Is the given item name a known item?
		if (item == null) {
			fail("Unknown item.");
			return false;
		}

		// Define the amount and init with default value if none given
		int amount = 1;

		// Only try to parse amount if the arg was given
		if (args.length > 1) {
			try {
				amount = Integer.parseInt(args[1]);
			}
			// This is the only exception we can expect
			catch (NumberFormatException ex) {
				fail("The amount you entered was not a valid number.");
				return false;
			}
			// Any other kind
			catch (Exception e) {
				fail("There was an unexpected error with the amount you entered. Please ask a "
						+ "server admin for help.");
				return false;
			}
		}

		// If somehow the amount ended up being 0 or lower anyway, then silently fail for now
		if (amount <= 0) {
			return true;
		}

		// Execute the order
		buy(item, amount);
		return true;
	}

	private void buy(Material item, int amount) {
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());
		ItemStack stack = new ItemStack(item, amount);
		String itemName = item.name().toLowerCase();
		BigDecimal rawCost = essentials.getWorth().getPrice(essentials, stack);

		if (rawCost == null) {
			fail("Could not find corresponding worth of this item. To fix, notify a server admin.");
			return;
		}

		double cost = rawCost.doubleValue()	* (1 + buyPriceIncrease);
		double bal = economy.getBalance(offlinePlayer);
		double totalCost = cost * amount;

		// Ensure the player can buy even one of this item
		if (bal < cost) {
			fail("You don't have enough money to buy this item.");
			alertCost(player, itemName, cost);
			return;
		}

		// Ensure the player can buy this exact amount
		if (bal < totalCost) {
			fail("You don't have enough money to buy that many of this item. The maximum you can "
							+ "buy right now is §e%sx %s§c.",
					(int) Math.floor(bal / cost), itemName);
			alertCost(player, itemName, cost);
			return;
		}

		// Withdraw the total cost of the player's balance
		EconomyResponse r = economy.withdrawPlayer(offlinePlayer, totalCost);

		// Send a message of either success or failure
		if (r.transactionSuccess()) {
			player.sendMessage(String.format("§aBought §e%sx %s§a for §2%s§a at §2%s§a each! You now have §2%s§a",
					amount, itemName, economy.format(r.amount), economy.format(cost),
					economy.format(r.balance)));
		} else {
			fail(r.errorMessage);
			return;
		}

		// Give the items to the player
		player.getInventory().addItem(stack);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender,
			Command command, String alias, String[] args) {
		List<String> answers = new ArrayList<>();
		if (args.length == 1) {
			for (Material material : Material.values()) {
				answers.add(material.name().toLowerCase());
			}
		} else {
			answers = Arrays.asList("<amount>");
		}
		return answers;
	}

	public String getLabel() {
		return label;
	}
}
