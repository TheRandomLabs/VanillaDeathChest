package com.therandomlabs.vanilladeathchest;

import java.util.Random;

import net.minecraft.item.DyeColor;

public enum ShulkerBoxColor {
	WHITE,
	ORANGE,
	MAGENTA,
	LIGHT_BLUE,
	YELLOW,
	LIME,
	PINK,
	GRAY,
	LIGHT_GRAY,
	CYAN,
	PURPLE,
	BLUE,
	BROWN,
	GREEN,
	RED,
	BLACK,
	RANDOM;

	private static final Random random = new Random();

	private final DyeColor color;

	ShulkerBoxColor() {
		color = "RANDOM".equals(name()) ? null : DyeColor.valueOf(name());
	}

	public DyeColor get() {
		return this == RANDOM ? DyeColor.byId(random.nextInt(16)) : color;
	}
}
