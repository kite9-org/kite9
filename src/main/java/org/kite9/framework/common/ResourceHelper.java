package org.kite9.framework.common;

import java.io.File;
import java.net.URL;

/**
 * Commonly you will want to keep test data along with the test java classes in the 
 * same directory, since this will make the tests portable accross testing environments.  
 * 
 * This class contains some common methods to help with this co-location.
 * @author robmoffat
 *
 */
public class ResourceHelper {


	
	public static File getHandleToFileInClasspath(Class<?> testClass, String name) {
	    String fullName = getFullFileName(testClass, name);
	    URL u = testClass.getClassLoader().getResource(fullName);
	    File file = new File(u.getFile());
	   
	    return file;
	}

	public static String getFullFileName(Class<?> testClass, String name) {
	    String packageName = testClass.getPackage().getName().replace(".", "/");
	    String className = testClass.getSimpleName();
	    String fullName = packageName+"/"+className+"/"+name;
	    return fullName;
	}
}
