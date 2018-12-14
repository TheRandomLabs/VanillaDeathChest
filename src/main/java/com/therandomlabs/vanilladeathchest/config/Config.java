package com.therandomlabs.vanilladeathchest.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Config {
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Ignore {}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Comment {
		String[] value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface LangKey {
		String value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface RangeInt {
		int min() default Integer.MIN_VALUE;

		int max() default Integer.MAX_VALUE;
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface RequiresMcRestart {}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface RequiresWorldRestart {}
}
