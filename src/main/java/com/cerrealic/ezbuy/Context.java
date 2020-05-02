package com.cerrealic.ezbuy;

import com.earth2me.essentials.Essentials;
import net.milkbowl.vault.economy.Economy;

class Context {
	private EzBuy plugin;
	private Essentials essentials;
	private Economy economy;

	public Context(EzBuy plugin, Essentials essentials, Economy economy) {
		this.plugin = plugin;
		this.essentials = essentials;
		this.economy = economy;
	}

	public Context() {
	}

	public void setPlugin(EzBuy plugin) {
		this.plugin = plugin;
	}

	public void setEssentials(Essentials essentials) {
		this.essentials = essentials;
	}

	public void setEconomy(Economy economy) {
		this.economy = economy;
	}

	public EzBuy getPlugin() {
		return plugin;
	}

	public Essentials getEssentials() {
		return essentials;
	}

	public Economy getEconomy() {
		return economy;
	}
}
