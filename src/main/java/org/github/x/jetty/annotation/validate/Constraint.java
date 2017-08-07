package org.github.x.jetty.annotation.validate;

public @interface Constraint {
	public abstract Class<?>[] validatedBy();
}
