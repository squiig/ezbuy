package com.cerrealic.ezbuy;

import com.cerrealic.cerspilib.Cerspi;
import com.cerrealic.cerspilib.logging.Debug;
import com.earth2me.essentials.Essentials;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class EzBuy extends JavaPlugin {
	public static final int RESOURCE_ID = 77802;

	@Override
	public void onEnable() {
		Cerspi.setContext(this, getServer());

		this.saveDefaultConfig();
		Context.config = this.getConfig();
//		Context.config.options().copyDefaults(true);

		if (Context.config.getBoolean("update-checking", false)) {
			Cerspi.checkForUpdates(RESOURCE_ID);
		}

		Debug.enabled = Context.config.getBoolean("debug", false);
		if (Debug.enabled) {
			getLogger().info("Debug enabled.");
		}

		if (!checkDependencies()) {
			return;
		}

		Context.essentials = (Essentials) getServer().getPluginManager().getPlugin("Essentials");

		Cerspi.registerCommand(CommandBuy.LABEL, new CommandBuy());

//		CommandBuy command = new CommandBuy();
//		PluginCommand pluginCommand = this.getCommand(CommandBuy.LABEL);
//		if (pluginCommand == null) {
//			getLogger().severe(String.format("Failed to register %s command!", CommandBuy.LABEL));
//			Cerspi.disablePlugin();
//			return;
//		}
//
//		pluginCommand.setExecutor(command);
//		pluginCommand.setTabCompleter(command);
	}

	boolean checkDependencies() {
		if (!Cerspi.isSpigotServer() && !Cerspi.isPaperServer()) {
			getLogger().severe("You're probably running a CraftBukkit server. For this to plugin to work you need to switch to Spigot or Paper.");
			Cerspi.disablePlugin();
			return false;
		}

		if (!tryLoadEconomy()) {
			getLogger().severe("Could not detect an economy service! Something probably went wrong with Vault.");
			Cerspi.disablePlugin();
			return false;
		}

		return true;
	}

	private boolean tryLoadEconomy() {
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

		if (rsp == null) {
			return false;
		}

		Context.economy = rsp.getProvider();
		return Context.economy != null;
	}
}
