package org.kite9.diagram.common;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class HelpMethods {

    public static <X> List<X> createList(X... d1) {
    	ArrayList<X> out = new ArrayList<X>();
    	for (X x : d1) {
    		out.add(x);
    	}
    	return out;
    }
    
    public static List<Element> listOf(Element... d1) {
    	ArrayList<Element> out = new ArrayList<Element>();
    	for (Element x : d1) {
    		out.add(x);
    	}
    	return out;
    }

}
