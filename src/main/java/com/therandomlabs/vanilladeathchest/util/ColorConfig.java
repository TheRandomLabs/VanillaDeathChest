package com.therandomlabs.vanilladeathchest.util;

import net.minecraft.item.EnumDyeColor;

public enum ColorConfig {
	WHITE("white"),
	ORANGE("orange"),
	MAGENTA("magenta"),
	LIGHT_BLUE("lightBlue"),
	YELLOW("yellow"),
	LIME("lime"),
	PINK("pink"),
	GRAY("gray"),
	SILVER("silver"),
	CYAN("cyan"),
	PURPLE("purple"),
	BLUE("blue"),
	BROWN("brown"),
	GREEN("green"),
	RED("red"),
	BLACK("black");

	private final String translationKey;
	private final EnumDyeColor color;

	ColorConfig(String translationKey) {
		this.translationKey = "vanilladeathchest.config.spawning.chestType.randomShulkerBoxColor." +
				translationKey;
		color = EnumDyeColor.valueOf(name());
	}

	@Override
	public String toString() {
		return translationKey;
	}

	public EnumDyeColor get() {
		return color;
	}
}
