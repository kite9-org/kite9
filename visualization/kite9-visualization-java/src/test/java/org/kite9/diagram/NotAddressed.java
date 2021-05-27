package org.kite9.diagram;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Means that the test has been logged, but since it hasn't been made to 
 * work you should expect it to fail.
 * 
 * Tests passed with this will fail (blue x) rather than error (red x).
 * @author robmoffat
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface NotAddressed {

	public String value() default "";
}
