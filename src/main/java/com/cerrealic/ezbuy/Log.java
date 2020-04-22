package com.cerrealic.ezbuy;

final class Log {
	static void info(String message, Object... formatArgs) {
		if (Context.lastUser == null) {
			return;
		}

		Context.lastUser.sendRawMessage(Format.info(message, formatArgs));
	}

	static void error(String message, Object... formatArgs) {
		if (Context.lastUser == null) {
			return;
		}

		Context.lastUser.sendRawMessage(Format.error(message, formatArgs));
	}

	static void success(String message, Object... formatArgs) {
		if (Context.lastUser == null) {
			return;
		}

		Context.lastUser.sendRawMessage(Format.success(message, formatArgs));
	}
}
