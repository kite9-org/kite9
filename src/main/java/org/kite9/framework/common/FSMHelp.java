package org.kite9.framework.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;

/**
 * Provides utility methods for checking that the before states of the FSM are met.
 * 
 * @author robmoffat
 *
 */
public class FSMHelp {

	/**
	 * Given an enum, returns true if the enum has the value specified in the beforestate annotation 'value' field.
	 */
	public static void stateOk(Class<? extends Annotation> beforeStateClass, Enum<?> currentValue) {
		stateOk(beforeStateClass, currentValue, "value");
	}
	
	/**
	 * Given an annotation field, returns true if the field 'fieldName' of the 'thisObject' is
	 * in the state given.
	 */
	public static void stateOk(Class<? extends Annotation> beforeStateClass, Enum<?> currentValue, String fieldName) {
		Method m;
		try {
			m = beforeStateClass.getDeclaredMethod(fieldName);
		} catch (SecurityException e) {
			throw new Kite9ProcessingException("Security issue accessing "+fieldName+" on annotation "+beforeStateClass, e);
		} catch (NoSuchMethodException e) {
			throw new Kite9ProcessingException("Annotation "+beforeStateClass+" does not have method named "+fieldName, e);
		}
		Method currentMethod = StackHelp.getAnnotatedMethod(beforeStateClass);
		Annotation ann = currentMethod.getAnnotation(beforeStateClass);
		Object values;
		try {
			values = m.invoke(ann);
		} catch (Exception e) {
			throw new Kite9ProcessingException("Could not determine annotation values", e);
		}
		if (values.getClass().isArray()) {
			for (int i = 0; i < Array.getLength(values); i++) {
				Object item = Array.get(values, i);
				if (item.equals(currentValue)) {
					return;
				}
			}
			throw new StateNotMatchingException(currentValue, (Enum<?>[])  values);
		} else {
			if (!values.equals(currentValue)) {
				throw new StateNotMatchingException(currentValue,(Enum<?>) values);
			}
		}
	}
}
