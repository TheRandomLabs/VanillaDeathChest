package com.therandomlabs.vanilladeathchest.config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import com.google.common.collect.ImmutableList;

public class Category {
	private final Object object;
	private final ImmutableList<Property> properties;
	private final String[] description;

	public Category(Field field, Object object, Field[] properties) throws IllegalAccessException {
		this.object = object;

		final List<Property> propertyList = new ArrayList<>();

		for(Field property : properties) {
			propertyList.add(new Property(property, property.get(object)));
		}

		this.properties = ImmutableList.copyOf(propertyList);
		description = field.getAnnotation(Config.Comment.class).value();
	}

	public Object getObject() {
		return object;
	}

	public ImmutableList<Property> getProperties() {
		return properties;
	}

	public String[] getDescription() {
		return description.clone();
	}
}
