package com.kite9.server.adl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.processors.AbstractInlineProcessor;
import org.kite9.diagram.logging.Kite9Log;
import org.kite9.diagram.logging.Kite9Log.Destination;
import org.kite9.diagram.logging.Kite9LogImpl;
import org.springframework.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * For ADL to be properly editable and transformable, the ids in the document 
 * must be unique.  This class ensures that is the case.
 * 
 * @author rob@kite9.com
 *
 */
public class IDFixer {
	
	static Random r = new Random();

	public static void main(String[] args) {
		Kite9Log.Companion.setFactory((l) -> new Kite9LogImpl(l));
		Kite9LogImpl.setLogging(Destination.OFF);
		String directoryOrFile = args[0];
		String extension = args.length > 1 ? args[1] : ".adl";
		File f = new File(directoryOrFile);
		fix(f, extension);
	}
	
	public static void fix(File f, String extension) {
		if(f.exists()) {
			if (f.isDirectory()) {
				fixDirectory(f, extension);
			} else {
				fixFile(f, extension);
			}
		} else {
			new FileNotFoundException(f.toString()).printStackTrace();
		}
	}

	private static void fixFile(File f, String extension) {
		if (f.getName().endsWith(extension)) {
			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				dbf.setNamespaceAware(true);
				DocumentBuilder db = dbf.newDocumentBuilder(); 
				Document d = db.parse(f);
				IDProcessor idp = new IDProcessor();
				idp.processContents(d);
				if (idp.changed) {
					FileWriter fw = new FileWriter(f);
					new XMLHelper().toXML(d, fw, false);
					fw.close();
				}
			
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.err.print("Not fixing: "+f.getName());
		}
	}

	private static void fixDirectory(File f, String extension) {
		Arrays.stream(f.listFiles()).forEach(f2 -> fix(f2, extension));
	}
	
	static class IDProcessor extends AbstractInlineProcessor {
		
		Set<String> foundIds = new HashSet<>();
		boolean changed = false;

		@Override
		protected Element processTag(Element n) {
			Attr id = n.getAttributeNode("id");
			if (id != null) {
				String checkId = checkId(id.getNodeValue());
				if (!checkId.equals(id.getNodeValue())) {
					id.setNodeValue(checkId);
					changed = true;
				}
				
				foundIds.add(checkId);
				
			}
			
			return super.processTag(n);
		}
		
		private char randomCharacter() {
			return (char) (97 + r.nextInt(26));
		}

		private String checkId(String id) {
			if (StringUtils.hasText(id)) {
				String in = id;
				while (foundIds.contains(in)) {
					in = id +"-"+randomCharacter()+randomCharacter();
				}
				return in;
			}
			return id;
		}
	}
}
