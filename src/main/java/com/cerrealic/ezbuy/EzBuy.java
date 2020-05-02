package com.cerrealic.ezbuy;

import com.cerrealic.cerspilib.Cerspi;
import com.cerrealic.cerspilib.logging.Debug;
import com.cerrealic.cerspilib.logging.Log;
import com.earth2me.essentials.Essentials;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class EzBuy extends JavaPlugin {
	public static final int RESOURCE_ID = 77802;

	@Override
	public void onEnable() {
		Cerspi.setContext(this, getServer());
		Context.plugin = this;
		initConfig();

		Debug.enabled = this.getConfig().getBoolean("debug", false);
		if (Debug.enabled) {
			getLogger().info("Debug enabled.");
		}

		Debug.target = Bukkit.getPlayer("StannuZ58");

		if (this.getConfig().getBoolean("update-checking", false)) {
			Cerspi.checkForUpdates(RESOURCE_ID);
		}

		if (!checkDependencies()) {
			return;
		}

		Context.essentials = (Essentials) getServer().getPluginManager().getPlugin("Essentials");

		Cerspi.registerCommand(CommandBuy.LABEL, new CommandBuy());
		Cerspi.registerCommand(CommandEzBuy.LABEL, new CommandEzBuy());
	}

	boolean assertPermission(Player player, String permission) {
		if (player.hasPermission(permission)) {
			return true;
		}
		Log.error("You don't have permission to use that command.");
		return false;
	}

	boolean assertPermissions(Player player, String... permissions) {
		for (String p : permissions) {
			if (!player.hasPermission(p)) {
				Log.error("You don't have permission to use that command.");
				return false;
			}
		}
		return true;
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

	private void initConfig() {
		this.getConfig().addDefault("debug", false);
		this.getConfig().addDefault("update-checking", true);
		this.getConfig().addDefault("cost-increase", 0.07d);
		this.getConfig().options().copyDefaults(true);
		this.saveDefaultConfig();
	}
}
