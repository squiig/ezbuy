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

		if (!trySetupEconomy()) {
			getLogger().severe("Could not detect an economy service! Something probably went "
					+ "wrong with Vault.");
			disablePlugin();
			return;
		}

		this.getCommand("buy").setExecutor(new CommandBuy(this));
		this.saveDefaultConfig();
	}

	@Override
	public void onDisable() {

	}

	public void disablePlugin() {
		getLogger().severe("Plugin disabled!");
		getServer().getPluginManager().disablePlugin(this);
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
