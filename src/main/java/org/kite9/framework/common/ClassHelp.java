package org.kite9.framework.common;


public class ClassHelp {

	/**
	 * Returns the loaded Class for a suite name.
	 * 
	 * @param classLoader
	 */
	public static Class<?> loadClass(String suiteClassName, ClassLoader classLoader) throws ClassNotFoundException {
		try {
			return classLoader.loadClass(suiteClassName);
		} catch (NullPointerException e) {
			throw new ClassNotFoundException("No class name given: "+suiteClassName, e);
		}
	}

	public static Object createInstance(Class<?> testClass) {
		try {
			Object out = testClass.newInstance();
			return out;
		} catch (InstantiationException e) {
			throw new Kite9ProcessingException("Could not instantiate: " + testClass, e);
		} catch (IllegalAccessException e) {
			throw new Kite9ProcessingException("Could not instantiate: " + testClass, e);
		}
	}
}
