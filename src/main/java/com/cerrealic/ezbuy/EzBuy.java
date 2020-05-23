package com.cerrealic.ezbuy;

import com.cerrealic.cerspilib.Cerspi;
import com.cerrealic.cerspilib.CerspiPlugin;
import com.cerrealic.cerspilib.config.CerspiPluginConfig;
import com.cerrealic.ezbuy.commands.CommandBuy;
import com.cerrealic.ezbuy.commands.CommandEzBuy;
import com.earth2me.essentials.Essentials;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
import org.bukkit.plugin.java.annotation.permission.ChildPermission;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.Website;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

@Plugin(name = "EzBuy", version = "b0.4.0-SNAPSHOT")
@Description("Simple but effective Spigot plugin that adds a /buy command.")
@Author("cerrealic")
@ApiVersion(ApiVersion.Target.v1_15)
@Website("https://www.spigotmc.org/resources/ezbuy.77802/")
@Dependency("Vault")
@Dependency("Essentials")
@org.bukkit.plugin.java.annotation.permission.Permissions(
		{
				@Permission(
						name = Permissions.COMMAND_ALL,
						desc = "Allows every command.",
						defaultValue = PermissionDefault.OP,
						children = {
								@ChildPermission(name = Permissions.COMMAND_EZBUY),
								@ChildPermission(name = Permissions.COMMAND_EZBUY_DEBUG),
								@ChildPermission(name = Permissions.COMMAND_EZBUY_PROFITRATE),
								@ChildPermission(name = Permissions.COMMAND_BUY)
						}
				)
		}
)
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

		Cerspi.registerCommands(this, true,
				new CommandBuy(this, context),
				new CommandEzBuy(this)
		);
	}

	@Override
	protected CerspiPluginConfig initConfig() {
		this.getConfig().options().copyDefaults(true);
		this.saveDefaultConfig();
		config = new EzBuyConfig(this, this.getConfig());
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
			Cerspi.disablePlugin(this);
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
