package org.kite9.framework;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates to the annotation processing runner that this method or type 
 * should be processed by the kite9 runner.
 * 
 * @author moffatr
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Kite9Item {

}
