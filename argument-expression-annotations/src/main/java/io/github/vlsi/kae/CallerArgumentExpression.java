package io.github.vlsi.kae;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks the parameter that should receive the caller argument expression for parameter specified by
 * {@link #value()}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface CallerArgumentExpression {
    /**
     * Name of the parameter to fetch the caller argument expression from.
     * @return name of the parameter to fetch the caller argument expression from
     */
    String value();
}
