package com.therandomlabs.vanilladeathchest.config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Property {
	private final String key;
	private final Field field;
	private final boolean isEnum;
	private final Object defaultValue;
	private final String[] description;

	private final int minInt;
	private final int maxInt;

	private final double minDouble;
	private final double maxDouble;

	public Property(Field field, Object defaultValue) {
		key = field.getName();
		this.field = field;
		this.defaultValue = defaultValue;

		final Class<?> type = field.getType();

		this.isEnum = Enum.class.isAssignableFrom(type);

		final List<String> description = new ArrayList<>(Arrays.asList(
				field.getAnnotation(Config.Property.class).value()
		));

		final Config.RangeInt rangeInt = field.getAnnotation(Config.RangeInt.class);

		if(rangeInt == null) {
			minInt = Integer.MIN_VALUE;
			maxInt = Integer.MAX_VALUE;
		} else {
			minInt = rangeInt.min();
			maxInt = rangeInt.max();

			description.add("Min: " + minInt);
			description.add("Max: " + maxInt);
		}

		final Config.RangeDouble rangeDouble = field.getAnnotation(Config.RangeDouble.class);

		if(rangeDouble == null) {
			minDouble = Double.MIN_VALUE;
			maxDouble = Double.MAX_VALUE;
		} else {
			minDouble = rangeDouble.min();
			maxDouble = rangeDouble.max();

			description.add("Min: " + minDouble);
			description.add("Max: " + maxDouble);
		}

		if(isEnum) {
			description.add("Valid values:");

			for(Object constant : type.getEnumConstants()) {
				description.add(constant.toString());
			}
		}

		description.add("Default: " + defaultValue);

		this.description = description.toArray(new String[0]);
	}

	public String getKey() {
		return key;
	}

	public Field getField() {
		return field;
	}

	public boolean isEnum() {
		return isEnum;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public String[] getDescription() {
		return description.clone();
	}

	public int getMinInt() {
		return minInt;
	}

	public int getMaxInt() {
		return maxInt;
	}

	public double getMinDouble() {
		return minDouble;
	}

	public double getMaxDouble() {
		return maxDouble;
	}
}
