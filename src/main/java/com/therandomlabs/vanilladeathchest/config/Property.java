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

	private final int min;
	private final int max;

	public Property(Field field, Object defaultValue) {
		key = field.getName();
		this.field = field;
		this.defaultValue = defaultValue;

		final Class<?> type = field.getType();

		this.isEnum = Enum.class.isAssignableFrom(type);

		final List<String> description = new ArrayList<>(Arrays.asList(
				field.getAnnotation(Config.Comment.class).value()
		));

		final Config.RangeInt rangeInt = field.getAnnotation(Config.RangeInt.class);

		if(rangeInt == null) {
			min = Integer.MIN_VALUE;
			max = Integer.MAX_VALUE;
		} else {
			min = rangeInt.min();
			max = rangeInt.max();

			description.add("Min: " + min);
			description.add("Max: " + max);
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

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}
}
