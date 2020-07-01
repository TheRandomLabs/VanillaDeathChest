package com.therandomlabs.vanilladeathchest.config;

import java.util.Random;

import net.minecraft.item.EnumDyeColor;

public enum ShulkerBoxColor {
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
	BLACK("black"),
	RANDOM("random");

	private static final Random random = new Random();

	private final String translationKey;
	private final EnumDyeColor color;

	ShulkerBoxColor(String translationKey) {
		this.translationKey = translationKey;
		color = "random".equals(translationKey) ? null : EnumDyeColor.valueOf(name());
	}

	@Override
	public String toString() {
		return "vanilladeathchest.config.spawning.chestType.shulkerBoxColor." + translationKey;
	}

	public EnumDyeColor get() {
		return this == RANDOM ? EnumDyeColor.byMetadata(random.nextInt(16)) : color;
	}
}
