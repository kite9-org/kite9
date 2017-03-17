package org.kite9.framework.common;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Compares two files and returns true if they are identical
 * 
 * @author robmoffat
 *
 */
public class FileDiff {

	public static void areFilesSame(File a, File b) throws IOException, DiffException {
		BufferedInputStream fisA = new BufferedInputStream(new FileInputStream(a));
		BufferedInputStream fisB = new BufferedInputStream(new FileInputStream(b));
		areFilesSame(a.getPath(), b.getPath(), fisA, fisB);
	
	}

	public static void areFilesSame(String a, String b, BufferedInputStream fisA, BufferedInputStream fisB)
			throws DiffException, IOException {
		int outA = 0;
		int outB = 0;
	
		do {
			if (outB != outA)
				throw new DiffException("Files are different "+a+" and "+b);
			
			outA = fisA.read();
			outB = fisB.read();
			
			
		} while ((outA != -1) && (outB != -1));
	}
	
	public static void filesContainSameLines(File a, File b) throws IOException, DiffException {
		filesContainSameLines(a, b, getDefaultComparator());
		
	}

	private static Comparator<String> getDefaultComparator() {
		return new Comparator<String>() {

			public int compare(String arg0, String arg1) {
				return arg0.compareTo(arg1);
			}
			
		};
	}
	
	/**
	 * Allows testing of file comparing where order of files is undefined
	 * @throws DiffException 
	 */
	public static void filesContainSameLines(File a, File b, Comparator<String> lc) throws IOException, DiffException {
		BufferedReader rA = new BufferedReader(new InputStreamReader(new FileInputStream(a)));
		BufferedReader rB = new BufferedReader(new InputStreamReader(new FileInputStream(b)));
		filesContainSameLines(rA, a.getPath(), rB, b.getPath(), lc);
	}
	
	public static void filesContainSameLines(BufferedReader rA, String pa, BufferedReader rB, String pb) throws IOException, DiffException {
		filesContainSameLines(rA, pa, rB, pb, getDefaultComparator());
	}
		
	public static void filesContainSameLines(BufferedReader rA, String pa, BufferedReader rB, String pb, Comparator<String> lc) throws IOException, DiffException {
		
		Set<String> aLines = new HashSet<String>();
		Set<String> bLines = new HashSet<String>();
		
		String line1 = removeProcessingInstructions(rA.readLine());
		while (line1!=null) {
			aLines.add(line1);
			line1 = rA.readLine();
		}
		
		line1 = removeProcessingInstructions(rB.readLine());
		while (line1!=null) {
			bLines.add(line1);
			line1 = rB.readLine();
		}
		
		// remove common lines
		for (String string : bLines) {
			boolean found = false;
			for (Iterator<?> iter = aLines.iterator(); iter.hasNext();) {
				String element = (String) iter.next();
				if (lc.compare(string, element)==0) {
					iter.remove();
					found = true;
					break;
				}
			}
			if (!found) {
				throw new DiffException("Not in "+pa+":"+string);
			}
		}
		
		for (String string : aLines) {
			throw new DiffException("Not in "+pb+":"+string);			
		}
	}
	
	static Pattern pattern = Pattern.compile("^\\<\\?.*\\?\\>");
	
	private static String removeProcessingInstructions(String readLine) {
		if (readLine==null)
			return null;
		 Matcher matcher = pattern.matcher(readLine);
	     String output = matcher.replaceAll("");
	     return output;
	}

	public static void fileContainsLines(File f, String... lines) throws IOException, DiffException {
	    BufferedReader rA = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
	    fileContainsLines(rA, f.getName(), lines);
	}
	
	
	public static void fileContainsLines(BufferedReader rA, String fn, String... lines) throws IOException, DiffException {
		StringBuilder aContent = new StringBuilder(10000);
		
		String line1 = rA.readLine();
		while (line1!=null) {
			aContent.append(line1);
			aContent.append("\n");
			line1 = rA.readLine();
		}
		
		String a = aContent.toString();
		
		// check lines are present
		for (String string : lines) {
			boolean found = a.contains(string.trim());
			
			if (!found) {
				throw new DiffException("Not in "+fn+":"+string);
			}
		}
	}
}
