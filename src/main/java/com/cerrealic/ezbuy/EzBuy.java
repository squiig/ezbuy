package com.cerrealic.ezbuy;

import com.earth2me.essentials.Essentials;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class EzBuy extends JavaPlugin {
	@Override
	public void onEnable() {
		if (!checkDependencies()) {
			return;
		}

		Context.essentials = (Essentials) getServer().getPluginManager().getPlugin("Essentials");
		Context.config = this.getConfig();

		this.saveDefaultConfig();

		Debug.enabled = this.getConfig().getBoolean("debug", false);
		if (Debug.enabled) {
			getLogger().info("Debug enabled.");
		}

		registerCommand();
	}

	boolean checkDependencies() {
		if (!isSpigotServer()) {
			getLogger()
					.severe("You're probably running a CraftBukkit server. For this to plugin to "
							+ "work you need to switch to Spigot AND use BungeeCord.");
			disablePlugin();
			return false;
		}

		if (!tryLoadEconomy()) {
			getLogger().severe("Could not detect an economy service! Something probably went "
					+ "wrong with Vault.");
			disablePlugin();
			return false;
		}

		return true;
	}

	void registerCommand() {
		PluginCommand command = this.getCommand(CommandBuy.LABEL);
		if (command == null) {
			getLogger().severe("Failed to register /buy command!");
			disablePlugin();
			return;
		}

		CommandBuy exec = new CommandBuy();
		command.setExecutor(exec);
		command.setTabCompleter(exec);
	}

	public void disablePlugin() {
		getServer().getPluginManager().disablePlugin(this);
	}

	private boolean tryLoadEconomy() {
		RegisteredServiceProvider<Economy> rsp =
				getServer().getServicesManager().getRegistration(Economy.class);

		if (rsp == null) {
			return false;
		}

		Context.economy = rsp.getProvider();
		return Context.economy != null;
	}

	private boolean isSpigotServer() {
		return getServer().getVersion().contains("Spigot");
	}
}
