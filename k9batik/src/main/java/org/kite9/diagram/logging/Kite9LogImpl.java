package org.kite9.diagram.logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Very simple abstraction class for all logging functions.  
 * 
 * @author robmoffat
 *
 */
public class Kite9LogImpl implements Kite9Log {

	private static String INDENT = new String(new char[100]).replace('\0', ' ');

	Logable logFor;
	
	static PrintStream logFile;

	public static void setLogging(Destination state) {
		if (state == Destination.FILE) {
			try {
				logFile = new PrintStream(new FileOutputStream(new File("kite9.log")));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				logFile = System.out;
			}
		} else if (state == Destination.STREAM) {
			logFile = System.out;
		} else {
			logFile = null;
		}
	}
	
	public boolean go() {
		return logFile == null;
	}

	public Kite9LogImpl(Logable o) {
		this.logFor = o;
	}

	public void send(String string) {
		if (logFor.isLoggingEnabled() && (logFile != null))
			logFile.println(logFor.getPrefix() + " " + string);
	}
	
	public void send(int indent, String string) {
		if (logFor.isLoggingEnabled() && (logFile != null))
			logFile.print(logFor.getPrefix());
			logFile.write(INDENT.getBytes(), 0, 1+indent);
			logFile.println(string);
	}

	public void send(String prefix, Collection<?> items) {
		if (logFor.isLoggingEnabled() && (logFile != null)) {
			logFile.println(logFor.getPrefix() + " " + prefix);

			StringBuffer sb = new StringBuffer();
			for (Object o : items) {
				sb.append("\t");
				sb.append(o.toString());
				sb.append("\n");
			}

			logFile.println(sb.toString());

		}
	}

	public void send(String prefix, Table t) {
		if (logFor.isLoggingEnabled() && (logFile != null)) {
			StringBuilder sb = new StringBuilder(1000);
			t.display(sb);
			send(prefix + sb.toString());
		}
	}

	public void send(String prefix, Map<?, ?> items) {
		if (logFor.isLoggingEnabled() && (logFile != null)) {
			logFile.println(logFor.getPrefix() + " " + prefix);
			Table t = new Table();
			Set<?> keys = items.keySet();
			List<Object> keyList = new ArrayList<Object>(keys);
			Collections.sort(keyList, new Comparator<Object>() {

				public int compare(Object o1, Object o2) {
					if (o1 == null) {
						return -1; 
					} else if (o2 == null) {
						return 1;
					} else {
						return o1.toString().compareTo(o2.toString());
					}
				}
				
			});

			for (Object object : keyList) {
				if (object != null) {
					t.addRow(new Object[] { "\t", object.toString(), items.get(object).toString() });
				}
			}

			StringBuilder sb = new StringBuilder();
			t.display(sb);
			logFile.println(sb.toString());
		}
	}

	public void error(String string) {
	    System.err.println(logFor.getPrefix()+" "+string);
	}

	public void error(String string, Throwable e) {
	    System.err.println(logFor.getPrefix()+" "+string);
	    e.printStackTrace();
	}

}
