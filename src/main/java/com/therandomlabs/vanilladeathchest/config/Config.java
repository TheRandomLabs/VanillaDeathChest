package com.therandomlabs.vanilladeathchest.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Config {
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Ignore {}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Category {
		String[] value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Property {
		String[] value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface RangeInt {
		int min() default Integer.MIN_VALUE;

		int max() default Integer.MAX_VALUE;
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface RangeDouble {
		double min() default Double.MIN_VALUE;

		double max() default Double.MAX_VALUE;
	}

	public @interface RequiresMCRestart {}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface RequiresWorldReload {}
}
