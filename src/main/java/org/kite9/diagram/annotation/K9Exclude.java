package org.kite9.diagram.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Allows you to exclude certain elements from a diagram.
 * 
 * @author robmoffat
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface K9Exclude {

    /**
     * Set this value to limit the scope of the annotation to specific diagrams
     */
    public Class<?>[] from() default { };
}
