package org.kite9.framework.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.kite9.framework.Kite9Item;

public class StackHelp {

	/**
	 * Finds the method corresponding to the {@link Kite9Item}.
	 */
	public static Method getKite9Item() {
		return getAnnotatedMethod(Kite9Item.class);
	}

	public static <X extends Annotation> Method getAnnotatedMethod(Class<X> ann) {
		StackTraceElement[] elems = Thread.currentThread().getStackTrace();
		for (int i = 0; i < elems.length; i++) {
			StackTraceElement e = elems[i];
			String name = e.getClassName();
			String methodName = e.getMethodName();
			List<Method> matchingMethods = getMethods(name, methodName);
			if (matchingMethods != null) {
				for (Method method : matchingMethods) {
					if (method.getAnnotation(ann) != null) {
						return method;
					}	
				}
			}
		}

		throw new StackSearchException(ann);
	}

	private static List<Method> getMethods(String name, String methodName) {
		List<Method> out = null;
		try {
			Class<?> cl = null;
			try {
				cl = Class.forName(name);
			} catch (Exception e) {
			}

			if (cl == null) {
				cl = Thread.currentThread().getContextClassLoader().loadClass(name);
			}

			Method[] m = cl.getDeclaredMethods();
			for (Method method : m) {
				if (method.getName().equals(methodName)) {
					if (out==null) {
						out = new LinkedList<Method>();
					}
					out.add(method);
				}
			}
			return out;
		} catch (Exception e) {
			return null;
		}
	}

}
