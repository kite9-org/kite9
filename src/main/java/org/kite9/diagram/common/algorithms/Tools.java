package org.kite9.diagram.common.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.kite9.framework.logging.LogicException;

/**
 * Static utility methods used by the compaction routines. 
 * 
 * @author robmoffat
 * 
 */
public class Tools {

	/**
	 * Adds a number of items into a pre-existing list at a certain index,
	 * replacing the existing index
	 * 
	 * @param face
	 * @param index
	 * @param first
	 * @param second
	 */
	@SuppressWarnings("unchecked")
	public static <X> void replaceInList(List<X> face, int index, X... itemsToAdd) {
		List<X> itemsAfter = (index < face.size() - 1) ? new ArrayList<X>(face.subList(index + 1, face.size()))
				: Collections.EMPTY_LIST;
		List<X> itemsBefore = (index == 0) ? Collections.EMPTY_LIST : new ArrayList<X>(face.subList(0, index));
		face.clear();
		face.addAll(itemsBefore);
		for (X x : itemsToAdd) {
			face.add(x);
		}
		face.addAll(itemsAfter);
	}

	/**
	 * Inserts objects into a list just after the given index.
	 * 
	 * @param <X>
	 * @param face
	 * @param index
	 * @param itemsToAdd
	 */
	@SuppressWarnings("unchecked")
	public static <X> void insertIntoList(List<X> face, int index, List<X> itemsToAdd) {
		index = (index + 1) % face.size(); // makes sure insert is after index,
		// not before

		int expectedSize = face.size() + itemsToAdd.size();
		List<X> itemsAfter = (index < face.size()) ? new ArrayList<X>(face.subList(index, face.size()))
				: Collections.EMPTY_LIST;
		List<X> itemsBefore = (index == 0) ? Collections.EMPTY_LIST : new ArrayList<X>(face.subList(0, index));
		face.clear();
		face.addAll(itemsBefore);
		for (X x : itemsToAdd) {
			face.add(x);
		}
		face.addAll(itemsAfter);
		if (face.size() != expectedSize)
			throw new LogicException("logic error. should be " + expectedSize + " was " + face.size());
	}

	public static <X> List<X> createList(X... d1) {
		ArrayList<X> out = new ArrayList<X>();
		for (X x : d1) {
			out.add(x);
		}
		return out;
	}

	public static <X> Set<X> createSet(X... d1) {
		LinkedHashSet<X> out = new LinkedHashSet<X>();
		for (X x : d1) {
			out.add(x);
		}
		return out;
	}
	
	
}
