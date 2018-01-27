package org.github.x.jetty.validate.annotation;

public @interface Constraint {
	public abstract Class<?>[] validatedBy();
}
