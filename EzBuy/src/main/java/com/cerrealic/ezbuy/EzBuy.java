package com.cerrealic.ezbuy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

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

		if (!serverHasVault()) {
			getLogger().severe("This plugin requires Vault, which you don't seem to have "
					+ "installed. Please install the Vault plugin.");
			disablePlugin();
			return;
		}

		if (!trySetupEconomy()) {
			getLogger().severe("Could not detect an economy service! Something probably went "
					+ "wrong with Vault.");
			disablePlugin();
			return;
		}

		this.getCommand("buy").setExecutor(new CommandBuy(this));
	}

	@Override
	public void onDisable() {

	}

	private void disablePlugin() {
		getLogger().severe("Plugin disabled!");
		getServer().getPluginManager().disablePlugin(this);
	}

	private boolean serverHasVault() {
		return getServer().getPluginManager().getPlugin("Vault") != null;
	}

	private boolean trySetupEconomy() {
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
}
