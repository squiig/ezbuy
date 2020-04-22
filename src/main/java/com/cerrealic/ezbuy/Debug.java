package com.cerrealic.ezbuy;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversable;

final class Debug {
	static boolean enabled;
	static Conversable target;

	static void info(String message, Object... formatArgs) {
		if (!enabled || target == null) {
			return;
		}

		Format.stripColors = target instanceof ConsoleCommandSender;
		target.sendRawMessage(Format.debug(message, formatArgs));
	}

	static void error(String message, Object... formatArgs) {
		if (!enabled) {
			return;
		}
		info(Format.error(message, formatArgs));
	}

	static void success(String message, Object... formatArgs) {
		if (!enabled) {
			return;
		}
		info(Format.success(message, formatArgs));
	}
}
