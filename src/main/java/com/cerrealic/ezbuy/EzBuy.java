package com.cerrealic.ezbuy;

import com.cerrealic.cerspilib.Cerspi;
import com.cerrealic.cerspilib.CerspiPlugin;
import com.cerrealic.cerspilib.config.CerspiPluginConfig;
import com.cerrealic.ezbuy.commands.CommandBuy;
import com.cerrealic.ezbuy.commands.CommandEzBuy;
import com.earth2me.essentials.Essentials;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

@Plugin(name = "EzBuy", version = "b0.4.0-SNAPSHOT")
@Description(value = "Simple but effective Spigot plugin that adds a /buy command.")
@Author(value = "cerrealic")
@ApiVersion(ApiVersion.Target.v1_15)
public class EzBuy extends CerspiPlugin {
	private static final int RESOURCE_ID = 77802;
	private EzBuyContext context;
	private EzBuyConfig config;

	public EzBuyContext getContext() {
		return context;
	}

	public static EzBuy getInstance() {
		return EzBuy.getPlugin(EzBuy.class);
	}

	@Override
	public Integer getResourceId() {
		return RESOURCE_ID;
	}

	@Override
	public void onEnable() {
		super.onEnable();
		context = new EzBuyContext((Essentials) getServer().getPluginManager().getPlugin("Essentials"), null);

		Cerspi.registerCommands(
				new CommandBuy(this, context),
				new CommandEzBuy(this)
		);
	}

	@Override
	protected CerspiPluginConfig initConfig() {
		this.getConfig().options().copyDefaults(true);
		this.saveDefaultConfig();
		config = new EzBuyConfig(this.getConfig());
		return config;
	}

	public EzBuyConfig getEzBuyConfig() {
		return config;
	}

	@Override
	protected boolean checkDependencies() {
		if (!super.checkDependencies()) {
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

		context.setEconomy(rsp.getProvider());
		return context.getEconomy() != null;
	}
}
