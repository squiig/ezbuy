package com.cerrealic.ezbuy;

import com.cerrealic.cerspilib.config.CerspiPluginConfig;
import com.cerrealic.cerspilib.config.ConfigNode;
import com.google.common.collect.Sets;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;

public class EzBuyConfig extends CerspiPluginConfig {
	private ConfigNode<Double> profitRate = new ConfigNode<>("profit-rate", 0.07d);

	public EzBuyConfig(FileConfiguration fileConfiguration) {
		super(fileConfiguration);
	}

	@Override
	protected HashSet<ConfigNode> getDefinedNodes() {
		return Sets.newHashSet(profitRate);
	}

	public double getProfitRate() {
		return profitRate.getValue();
	}

	public void setProfitRate(double profitRate) {
		setNodeValue(this.profitRate, profitRate);
	}
}
