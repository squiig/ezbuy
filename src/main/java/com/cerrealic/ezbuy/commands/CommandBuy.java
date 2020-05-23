package com.cerrealic.ezbuy.commands;

import com.cerrealic.cerspilib.CerspiCommand;
import com.cerrealic.cerspilib.logging.Debug;
import com.cerrealic.cerspilib.logging.Format;
import com.cerrealic.cerspilib.logging.Log;
import com.cerrealic.ezbuy.EzBuy;
import com.cerrealic.ezbuy.EzBuyContext;
import com.earth2me.essentials.IEssentials;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandBuy extends CerspiCommand {
	private static final String LABEL = "buy";
	private IEssentials essentials;
	private Economy economy;
	private double costIncrease;

	public CommandBuy(EzBuy plugin, EzBuyContext context) {
		this.essentials = context.getEssentials();
		this.economy = context.getEconomy();
		costIncrease = plugin.getConfig().getDouble("cost-increase");
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!isPlayer(sender)) {
			sender.sendMessage(ChatColor.YELLOW + "The /buy command is only available to players!");
			return true;
		}

		Player player = (Player) sender;
		Log.target = Debug.target = player;

		if (!assertValidArgs(args))
			return false;

		Material item = Material.matchMaterial(args[0]);

		// Is the given item name a valid item?
		if (item == null) {
			fail("Unknown item.");
			return false;
		}

		int amountAsked = 1;

		// Only try to parse amount if the arg was given
		if (args.length > 1) {
			try {
				amountAsked = Integer.parseInt(args[1]);
			}
			catch (NumberFormatException ex) {
				fail("The amount you entered was not a valid number.");
				return false;
			}
			catch (Exception e) {
				fail("There was an unexpected error with the amount you entered. Please ask a server admin for help.");
				return false;
			}
		}

		if (amountAsked <= 0) {
			return false;
		}

		buy(player, new ItemStack(item, amountAsked));
		return true;
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

	public void buy(Player player, ItemStack stack) {
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());
		Material orderedMaterial = stack.getType();
		int orderedAmount = stack.getAmount();

		if (!canBuy(offlinePlayer, stack)) {
			return;
		}

		double totalPrice = getPrice(stack);
		double singlePrice = getPrice(new ItemStack(stack.getType(), 1));

		EconomyResponse response = economy.withdrawPlayer(offlinePlayer, totalPrice);
		if (response.transactionSuccess()) {
			Log.success("Bought %s&a for %s&a at %s&a each! You now have %s", Format.stack(new ItemStack(orderedMaterial, orderedAmount)), Format.money(response.amount), Format.money(singlePrice),
					Format.money(response.balance));
		}
		else {
			fail(response.errorMessage);
			return;
		}

		// Give the items to the player
		player.getInventory().addItem(new ItemStack(orderedMaterial, orderedAmount));
	}

	private boolean canBuy(OfflinePlayer player, ItemStack stack) {
		double singlePrice = getPrice(new ItemStack(stack.getType(), 1));

		if (singlePrice < 0) {
			return false;
		}

		double totalPrice = getPrice(stack);
		double bal = economy.getBalance(player);

		// Ensure the player can buy even one of this item
		if (bal < singlePrice) {
			fail("You don't have enough money to buy this item.");
			alertCost(stack.getType(), singlePrice);
			return false;
		}

		// Ensure the player can buy this exact amount
		if (bal < totalPrice) {
			int buyableAmount = (int) Math.floor(bal / singlePrice);
			fail("You can't afford that many. The most you can buy right now is %s&c which costs %s&c.", Format.stack(new ItemStack(stack.getType(), buyableAmount)), Format.money(buyableAmount * singlePrice));
			alertCost(stack.getType(), singlePrice);
			if (stack.getAmount() > 1) {
				alertCost(stack, singlePrice);
			}
			return false;
		}

		return true;
	}

	private double getPrice(ItemStack stack) {
		BigDecimal price = essentials.getWorth().getPrice(essentials, new ItemStack(stack.getType()));

		if (price == null) {
			fail("Could not find corresponding worth of this item. To fix, notify a server admin.");
			return -1;
		}

		return price.doubleValue() * (1 + costIncrease) * stack.getAmount();
	}

	private boolean isPlayer(CommandSender sender) {
		return sender instanceof Player;
	}

	private boolean assertValidArgs(String[] args) {
		return (args.length > 0 && args.length < 3);
	}

	private void alertCost(Material material, double cost) {
		Log.info("One of %s&6 currently costs %s", Format.material(material), Format.money(cost));
	}

	private void alertCost(ItemStack stack, double cost) {
		Log.info("%s&6 currently costs %s", Format.stack(stack), Format.money(cost * stack.getAmount()));
	}

	private void fail(String message, Object... formatArgs) {
		Log.error("Purchase failed: " + message, formatArgs);
	}
}
