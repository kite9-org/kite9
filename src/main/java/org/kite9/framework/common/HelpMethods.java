package org.kite9.framework.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kite9.diagram.dom.elements.Kite9XMLElement;

public class HelpMethods {

    public static <X> List<X> createList(X... d1) {
    	ArrayList<X> out = new ArrayList<X>();
    	for (X x : d1) {
    		out.add(x);
    	}
    	return out;
    }
    
    public static List<Kite9XMLElement> listOf(Kite9XMLElement... d1) {
    	ArrayList<Kite9XMLElement> out = new ArrayList<Kite9XMLElement>();
    	for (Kite9XMLElement x : d1) {
    		out.add(x);
    	}
    	return out;
    }

    public static <X> Set<X> createSet(X... d1) {
    	HashSet<X> out = new HashSet<X>();
    	for (X x : d1) {
    		out.add(x);
    	}
    	return out;
    }

}
