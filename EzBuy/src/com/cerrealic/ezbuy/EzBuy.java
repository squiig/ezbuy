package com.cerrealic.ezbuy;

import org.bukkit.plugin.java.JavaPlugin;

public class EzBuy extends JavaPlugin {

	@Override
	public void onEnable() {
		if (!isSpigotServer()) {
			getLogger().severe("Plugin disabled!");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
        this.getCommand("buy").setExecutor(new CommandBuy());
	}

	@Override
	public void onDisable() {

	}

	private boolean isSpigotServer() {
		if (getServer().getVersion().contains("Spigot"))
			return true;
		
		getLogger().severe("You're probably running a CraftBukkit server. For this to plugin to work you need to switch to Spigot AND use BungeeCord.");
		return false;
	}
}
