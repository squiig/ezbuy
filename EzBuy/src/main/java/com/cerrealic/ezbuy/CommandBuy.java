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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandBuy implements CommandExecutor, TabCompleter {
	private EzBuy plugin;
	private Economy economy;
	private IEssentials essentials;
	private String label;

	public CommandBuy(EzBuy plugin) {
		this.plugin = plugin;
		label = "buy";
		economy = plugin.getEconomy();
		essentials = plugin.getEssentials();
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

		// Is the given item name a real item?
		if (Material.matchMaterial(args[0]) == null) {
			sender.sendMessage("Buying failed: That's not a valid item.");
			return false;
		}

		Material item = Material.matchMaterial(args[0]);
		int amount = 1;

		if (args.length > 1) {
			try {
				amount = Integer.parseInt(args[1]);
			} catch (NumberFormatException ex) {
				sender.sendMessage("Buying failed: The amount you entered was not a valid number.");
				return false;
			} catch (Exception e) {
				sender.sendMessage(
						"Buying failed: There was an unexpected error with the amount you"
								+ " entered. Please ask a server admin for help.");
				return false;
			}
		}

		if (amount <= 0) {
			return true;
		}

		buy(player, item, amount);
		return true;
	}

	private void buy(Player player, Material item, int amount) {
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());
		ItemStack stack = new ItemStack(item, amount);
		double cost = essentials.getWorth().getPrice(essentials, stack).doubleValue();
		double bal = economy.getBalance(offlinePlayer);

		if (bal < cost) {
			player.sendMessage("Buying failed: You don't have enough money to buy this item.");
			return;
		}

		if (bal < amount * cost) {
			player.sendMessage(String.format("Buying failed: You don't have enough money to buy that "
					+ "many of this item. The maximum you can buy right now is %s.",
					Math.floor(bal / cost)));
			return;
		}

		EconomyResponse r = economy.withdrawPlayer(offlinePlayer, cost);

		if(r.transactionSuccess()) {
			player.sendMessage(String.format("You paid %s and now have %s",
					economy.format(r.amount), economy.format(r.balance)));
		} else {
			player.sendMessage(String.format("An error occurred: %s", r.errorMessage));
		}

		player.getInventory().addItem(stack);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender,
			Command command, String alias, String[] args) {
		return Stream.of(Material.values()).map(Material::name).collect(Collectors.toList());
	}

	public String getLabel() {
		return label;
	}
}
