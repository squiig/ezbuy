package com.cerrealic.ezbuy;

import com.earth2me.essentials.Essentials;
import net.milkbowl.vault.economy.Economy;

public class EzBuyContext {
	private Essentials essentials;
	private Economy economy;

	public EzBuyContext(Essentials essentials, Economy economy) {
		this.essentials = essentials;
		this.economy = economy;
	}

	public void setEssentials(Essentials essentials) {
		this.essentials = essentials;
	}

	public void setEconomy(Economy economy) {
		this.economy = economy;
	}

	public Essentials getEssentials() {
		return essentials;
	}

	public Economy getEconomy() {
		return economy;
	}
}
