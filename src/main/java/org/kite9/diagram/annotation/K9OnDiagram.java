package org.kite9.diagram.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface K9OnDiagram {

    /**
     * Set this value to limit the scope of the annotation to specific diagrams
     */
    public Class<?>[] on() default { };
    
    /**
     * Set this if you want to override the alias that will be produced for the item
     * by the aliaser.
     */
    public String alias() default "";
    
    /**
     * Allows you to set the stereotype for a class, method, package etc.
     */
    public String stereotype() default "";
}
