package com.cerrealic.ezbuy;

import com.earth2me.essentials.Essentials;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.conversations.Conversable;

public class Context {
	static FileConfiguration config;
	static Essentials essentials;
	static Economy economy;
	static Conversable lastUser;
}
