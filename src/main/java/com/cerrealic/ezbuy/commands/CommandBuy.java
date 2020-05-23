package com.cerrealic.ezbuy.commands;

import com.cerrealic.cerspilib.CerspiCommand;
import com.cerrealic.cerspilib.logging.Formatter;
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
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.permission.Permissions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Commands(
		@org.bukkit.plugin.java.annotation.command.Command(
				name = "buy",
				desc = "Buys items from the server using your balance and the defined worth of the item.",
				usage = "/<command> <item> <amount>",
				aliases = {"purchase", "order"},
				permission = com.cerrealic.ezbuy.Permissions.COMMAND_BUY
		)
)
@Permissions(
		@Permission(
				name = com.cerrealic.ezbuy.Permissions.COMMAND_BUY,
				desc = "Allows the buying of items with /buy.",
				defaultValue = PermissionDefault.TRUE
		)
)
public class CommandBuy extends CerspiCommand {
	private static final String LABEL = "buy";
	private IEssentials essentials;
	private Economy economy;
	private double profitRate;
	private EzBuy ezBuy;

	public CommandBuy(EzBuy ezBuy, EzBuyContext context) {
		this.ezBuy = ezBuy;
		this.essentials = context.getEssentials();
		this.economy = context.getEconomy();
		profitRate = ezBuy.getEzBuyConfig().getProfitRate();
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
		ezBuy.getCerspiLogger().setTarget(player);

		if (!assertValidArgs(args)) {
			return false;
		}

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
			} catch (NumberFormatException ex) {
				fail("The amount you entered was not a valid number.");
				return false;
			} catch (Exception e) {
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
			ezBuy.getCerspiLogger().log(new Formatter("Bought %s&a for %s&a at %s&a each! You now have %s")
							.format(
									Formatter.stylizeStack(new ItemStack(orderedMaterial, orderedAmount)),
									economy.format(response.amount),
									economy.format(singlePrice),
									economy.format(response.balance))
							.stylizeSuccess().toString(),
					false);
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
			fail("You can't afford that many. The most you can buy right now is %s&c which costs %s&c.",
					Formatter.stylizeStack(new ItemStack(stack.getType(), buyableAmount)),
					economy.format(buyableAmount * singlePrice));
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

		return price.doubleValue() * (1 + profitRate) * stack.getAmount();
	}

	private boolean isPlayer(CommandSender sender) {
		return sender instanceof Player;
	}

	private boolean assertValidArgs(String[] args) {
		return (args.length > 0 && args.length < 3);
	}

	private void alertCost(Material material, double cost) {
		ezBuy.getCerspiLogger().log(
				new Formatter("One of %s&6 currently costs %s")
						.format(
								Formatter.stylizeMaterial(material),
								economy.format(cost))
						.stylizeInfo().toString(),
				false);
	}

	private void alertCost(ItemStack stack, double cost) {
		ezBuy.getCerspiLogger().log(
				new Formatter("%s&6 currently costs %s")
						.format(
								Formatter.stylizeStack(stack),
								economy.format(cost * stack.getAmount()))
						.stylizeInfo().toString(),
				false);
	}

	private void fail(String message, Object... formatArgs) {
		ezBuy.getCerspiLogger().log(
				new Formatter("Purchase failed: " + message)
						.format(formatArgs)
						.stylizeError().toString(),
				false);
	}
}
