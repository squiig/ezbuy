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

import java.util.Map;

public class CommandBuy implements CommandExecutor {
	private EzBuy plugin;
	private Economy economy;
	private Map<String, Float> worthMap;

	public CommandBuy(EzBuy plugin) {
		this.plugin = plugin;
		economy = plugin.getEconomy();

		if (!tryGetWorthData()) {
			plugin.getLogger().severe("Could not read Essentials' worth.yml file! Please make "
					+ "sure it's there, otherwise this plugin doesn't have much use.");
			plugin.disablePlugin();
		}
	}

	private boolean tryGetWorthData() {
		//plugin.getServer().getPluginManager().getPlugin("Essentials").getResource("worth.yml");
		return true;
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
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());

		//sender.sendMessage(String.format("You have %s", economy.format(economy.getBalance
		// (player))));

		// Is the given item name a real item?
		if (Material.matchMaterial(args[0]) == null) {
			sender.sendMessage("Buying failed: That's not a valid item.");
			return false;
		}

		Material item = Material.matchMaterial(args[0]);
		int amount = 1;

		try {
			amount = Integer.parseInt(args[1]);
		} catch (NumberFormatException ex) {
			sender.sendMessage("Buying failed: The amount you entered was not a valid number.");
			return false;
		} catch (Exception e) {
			sender.sendMessage("Buying failed: There was an unexpected error with the amount you"
					+ " entered. Please ask a server admin for help.");
			return false;
		}

		if (amount <= 0) {
			return true;
		}

		EconomyResponse r = economy.withdrawPlayer(offlinePlayer, amount);

		if(r.transactionSuccess()) {
			sender.sendMessage(String.format("You were given %s and now have %s",
					economy.format(r.amount), economy.format(r.balance)));
		} else {
			sender.sendMessage(String.format("An error occurred: %s", r.errorMessage));
		}

		ItemStack stack = new ItemStack(item, amount);
		player.getInventory().addItem(stack);

		return true;
	}

}
