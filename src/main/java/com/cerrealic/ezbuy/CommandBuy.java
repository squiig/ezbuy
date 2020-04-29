package com.cerrealic.ezbuy;

import com.cerrealic.cerspilib.Debug;
import com.cerrealic.cerspilib.Format;
import com.cerrealic.cerspilib.Log;
import com.earth2me.essentials.IEssentials;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import java.util.Collections;
import java.util.List;

public class CommandBuy implements CommandExecutor, TabCompleter {
	public static final String LABEL = "buy";
	private Economy economy;
	private IEssentials essentials;
	private double buyPriceIncrease;

	public CommandBuy() {
		economy = Context.economy;
		essentials = Context.essentials;
		buyPriceIncrease = Context.config.getDouble("cost-increase");
	}

	private void alertCost(Material material, double cost) {
		Log.info("One of %s&6 currently costs %s", Format.material(material), Format.money(cost));
	}

	private void alertCost(ItemStack stack, double cost) {
		Log.info("%s&6 currently costs %s", Format.item(stack), Format.money(cost * stack.getAmount()));
	}

	private void fail(String message, Object... formatArgs) {
		Log.error("Purchase failed: " + message, formatArgs);
	}

	private double getCost(Material item) {
		BigDecimal rawCost = essentials.getWorth().getPrice(essentials, new ItemStack(item));

		if (rawCost == null) {
			fail("Could not find corresponding worth of this item. To fix, notify a server admin.");
			return -1;
		}

		return rawCost.doubleValue() * (1 + buyPriceIncrease);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// Ensure the sender is a player and not a console or command block
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.YELLOW + "The /buy command is only available to players!");
			return true;
		}

		Context.lastUser = Log.target = Debug.target = (Player) sender;

		// Ensure correct arg amount
		if (args.length == 0 || args.length > 2) {
			return false;
		}

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
				fail("There was an unexpected error with the amount you entered. Please ask a server admin for help.");
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

	private void buy(Material material, int itemAmount) {
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(((Player) Context.lastUser).getUniqueId());
		double cost = getCost(material);

		if (cost < 0) {
			return;
		}

		double bal = economy.getBalance(offlinePlayer);
		double totalCost = cost * itemAmount;

		// Ensure the player can buy even one of this item
		if (bal < cost) {
			fail("You don't have enough money to buy this item.");
			alertCost(material, cost);
			return;
		}

		// Ensure the player can buy this exact amount
		if (bal < totalCost) {
			fail("You don't have enough money to buy that many of this item. The maximum you can buy right now is &e%sx %s&c.", Format.amount((int) Math.floor(bal / cost)),
					Format.material(material));
			alertCost(material, cost);
			alertCost(new ItemStack(material, itemAmount), cost);
			return;
		}

		// Withdraw the total cost of the player's balance
		EconomyResponse r = economy.withdrawPlayer(offlinePlayer, totalCost);

		// Send a message of either success or failure
		if (r.transactionSuccess()) {
			Log.success("Bought %s&a for %s&a at %s&a each! You now have %s", Format.item(new ItemStack(material, itemAmount)), Format.money(r.amount), Format.money(cost),
					Format.money(r.balance));
		}
		else {
			fail(r.errorMessage);
			return;
		}

		// Give the items to the player
		((Player) Context.lastUser).getInventory().addItem(new ItemStack(material, itemAmount));
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> itemNames = new ArrayList<>();
		switch (args.length) {
			case 1:
				for (Material material : Material.values()) {
					if (material.isItem() && essentials.getWorth().getPrice(essentials, new ItemStack(material)) != null) {
						itemNames.add(material.name().toLowerCase());
					}
				}
				break;
			case 2:
				itemNames = Collections.singletonList("<amount>");
				break;
			default:
				return null;
		}

		// return unfiltered
		if (args[0].isEmpty()) {
			return itemNames;
		}

		// filter based on current input
		String[] result = itemNames.stream()
				.filter((name) -> name.startsWith(args[0]))
				.toArray(String[]::new);

		return Arrays.asList(result);
	}
}
