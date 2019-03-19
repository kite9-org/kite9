package org.kite9.diagram.adl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kite9.diagram.adl.Symbol.SymbolShape;
import org.kite9.diagram.dom.elements.ADLDocument;
import org.kite9.diagram.dom.elements.AbstractStyledKite9XMLElement;

/**
 * Helps in the creation of keys by making sure that symbols don't reuse the
 * same code
 * 
 * @author robmoffat
 * 
 */
public class KeyHelper {

	static class UsedKey {

		SymbolShape shape;

		char theChar;

		@Override
		public boolean equals(Object arg0) {
			if (arg0 instanceof UsedKey) {
				UsedKey uk = (UsedKey) arg0;
				return this.shape.equals(uk.shape) && this.theChar == uk.theChar;
			} else {
				return false;
			}

		}

		public String toString() {
			return shape.name() + ":" + theChar;
		}

		@Override
		public int hashCode() {
			return theChar + shape.hashCode();
		}

	}

	public List<Symbol> getUsedSymbols() {
		List<Symbol> out = new ArrayList<Symbol>(declared.values());
	//	Collections.sort(out);
		return out;
	}

	Set<UsedKey> used = new HashSet<UsedKey>();
	Map<String, Symbol> declared = new HashMap<String, Symbol>();
	private ADLDocument doc;
	
	public KeyHelper() {
		this(AbstractMutableXMLElement.TESTING_DOCUMENT);
	}

	public KeyHelper(ADLDocument doc) {
		this.doc = doc;
	}

	public Symbol createSymbol(String text) {
		return createSymbol(text, text);
	}

	public Symbol createSymbol(String text, String characterOptions) {
		Symbol out = declared.get(text);
		if (out != null)
			return out;

		if (characterOptions != null) {
			for (int i = 0; i < characterOptions.length(); i++) {
				char potential = Character.toUpperCase(characterOptions.charAt(i));
				Symbol s = findAvailableShape(text, potential);
				if (s != null)
					return s;
			}
		}

		for (int i = 0; i < text.length(); i++) {
			char potential = Character.toUpperCase(text.charAt(i));
			Symbol s = findAvailableShape(text, potential);
			if (s != null)
				return s;
		}

		// ok, nothing relevant. return first available symbol
		for (char c = 'A'; c <= 'Z'; c++) {
			Symbol s = findAvailableShape(text, c);
			if (s != null)
				return s;
		}

		// no shapes left to add, something has gone very wrong!
		throw new IllegalStateException("There are no more keys to allocate! " + used.size() + " used already.");
	}

	private Symbol findAvailableShape(String text, char potential) {
		for (SymbolShape shape : SymbolShape.values()) {
			Symbol out = findAvailableShape(text, potential, shape);

			if (out != null) {
				declared.put(text, out);
				return out;
			}
		}
		return null;
	}

	private Symbol findAvailableShape(String text, char potential, SymbolShape shape) {
		UsedKey option = new UsedKey();
		option.shape = shape;
		option.theChar = potential;
		if (!used.contains(option)) {
			Symbol s = new Symbol(text, potential, shape, doc);
			used.add(option);
			return s;
		}

		return null;
	}

	public Symbol createSymbol(String value, char key, SymbolShape shape) {
		Symbol out = findAvailableShape(value, key, shape);
		if (out != null) {
			return out;
		}
		out = findAvailableShape(value, key);
		if (out != null) {
			return out;
		}

		return createSymbol(value, null);
	}

}
