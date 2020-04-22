package com.cerrealic.ezbuy;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;

final class Format {
	static boolean stripColors;

	static String colors(String text, Object... formatArgs) {
		String formattedText = String.format(text.replace("NULL", Format.undefined()), formatArgs) + ChatColor.RESET;
		String result = ChatColor.translateAlternateColorCodes('&', formattedText);
		return stripColors ? ChatColor.stripColor(result) : result;
	}

	static String info(String text, Object... formatArgs) {
		return colors(ChatColor.GOLD + text, formatArgs);
	}

	static String debug(String text, Object... formatArgs) {
		return colors(ChatColor.LIGHT_PURPLE + "[DEBUG] " + ChatColor.DARK_PURPLE + text, formatArgs);
	}

	static String error(String text, Object... formatArgs) {
		return colors(ChatColor.RED + text, formatArgs);
	}

	static String success(String text, Object... formatArgs) {
		return colors(ChatColor.GREEN + text, formatArgs);
	}

	static String amount(int amount) {
		return colors(ChatColor.AQUA + Integer.toString(amount));
	}

	static String money(double amount) {
		return Context.economy.format(amount);
	}

	static String money(BigDecimal amount) {
		if (amount == null) {
			return "NULL";
		}

		return money(amount.doubleValue());
	}

	static String material(Material material) {
		if (material == null) {
			return "NULL";
		}
		return colors(ChatColor.YELLOW + StringUtils.capitalize(material.name().toLowerCase().replace('_', ' ')));
	}

	static String item(ItemStack stack) {
		if (stack == null) {
			return colors("NULL");
		}
		return colors("%sx&e%s", amount(stack.getAmount()), stack.getType().name());
	}

	private static String undefined() {
		return "&4&lNULL&r";
	}
}
