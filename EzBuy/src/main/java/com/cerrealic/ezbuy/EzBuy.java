package com.cerrealic.ezbuy;

import com.earth2me.essentials.Essentials;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;

public class EzBuy extends JavaPlugin {

	private static Economy economy = null;

	@Override
	public void onEnable() {
		if (!isSpigotServer()) {
			getLogger()
					.severe("You're probably running a CraftBukkit server. For this to plugin to "
							+ "work you need to switch to Spigot AND use BungeeCord.");
			disablePlugin();
			return;
		}

		if (!tryLoadEconomy()) {
			getLogger().severe("Could not detect an economy service! Something probably went "
					+ "wrong with Vault.");
			disablePlugin();
			return;
		}

		CommandBuy buy = new CommandBuy(this);
		this.getCommand(buy.getLabel()).setExecutor(buy);
		this.getCommand(buy.getLabel()).setTabCompleter(buy);
		this.saveDefaultConfig();
	}

	@Override
	public void onDisable() {

	}

	public void disablePlugin() {
		getLogger().severe("Plugin disabled!");
		getServer().getPluginManager().disablePlugin(this);
	}

	private boolean tryLoadEconomy() {
		RegisteredServiceProvider<Economy> rsp =
				getServer().getServicesManager().getRegistration(Economy.class);

		if (rsp == null) {
			return false;
		}

		economy = rsp.getProvider();
		return economy != null;
	}

	private boolean isSpigotServer() {
		return getServer().getVersion().contains("Spigot");
	}

	public static Economy getEconomy() {
		return economy;
	}

	public Essentials getEssentials() {
		return (Essentials) getServer().getPluginManager().getPlugin("Essentials");
	}

	public static String formatColors(String text, Object... formatArgs) {
		return String.format(ChatColor.translateAlternateColorCodes('&', text), formatArgs);
	}

	public static String formatMoney(double amount) {
		return formatColors("&2%s&r", economy.format(amount));
	}
}
