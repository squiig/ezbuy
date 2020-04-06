package com.cerrealic.ezbuy;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandBuy implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("The /buy command is only available to players.");
			return true;
		}
		
		Player player = (Player) sender;
		int amount = -1;
		
		try {
			amount = Integer.parseInt(args[1]);	
		} catch (NumberFormatException ex) {
			sender.sendMessage("Buying failed: The amount you entered was not a valid number.");
			return false;
		} catch (Exception e) {
			sender.sendMessage("Buying failed: There was an unexpected error with the amount you entered. Please ask a server admin for help.");
			return false;
		}
		
		if (amount < 0) {
			return true;
		}
		
		ItemStack diamonds = new ItemStack(Material.DIAMOND, amount);
		player.getInventory().addItem(diamonds);
		return true;
	}

}
