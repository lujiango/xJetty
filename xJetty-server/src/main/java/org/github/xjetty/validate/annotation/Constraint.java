package org.github.xjetty.validate.annotation;

public @interface Constraint {
	public abstract Class<?>[] validatedBy();
}
