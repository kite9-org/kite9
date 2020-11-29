package org.kite9.diagram.common.algorithms.det;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * This prevents any attempts to order the contents, as the order will always 
 * be non-deterministic if we use objects that don't declare hashCode.  
 * 
 * Use this in preference to HashSet, unless ordering is required, and in which case use
 * LinkedHashSet or DetHashSet.
 * 
 * @author robmoffat
 *
 */
public class UnorderedSet<K> extends HashSet<K>{

	public UnorderedSet() {
		super();
	}

	public UnorderedSet(Collection<? extends K> c) {
		super(c);
	}

	public UnorderedSet(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	public UnorderedSet(int initialCapacity) {
		super(initialCapacity);
	}

	@Override
	public Stream<K> stream() {
		throw new UnsupportedOperationException("Prevented By UnorderedSet");
	}

	@Override
	public Stream<K> parallelStream() {
		throw new UnsupportedOperationException("Prevented By UnorderedSet");
	}

	@Override
	public void forEach(Consumer<? super K> action) {
		throw new UnsupportedOperationException("Prevented By UnorderedSet");
	}

	@Override
	public Iterator<K> iterator() {
		throw new UnsupportedOperationException("Prevented By UnorderedSet");
	}

	@Override
	public Spliterator<K> spliterator() {
		throw new UnsupportedOperationException("Prevented By UnorderedSet");
	}

	/**
	 * Use sparingly - provided for logging only.
	 */
	@Override
	public Object[] toArray() {
		Object[] r = new Object[size()];
        int i = 0;
        for (Iterator<K> iterator = super.iterator(); iterator.hasNext();) {
			K type = iterator.next();
			r[i] = type;
			i++;
		}
        
        return r;
	}
	
	 public boolean retainAll(Collection<?> c) {
	        Objects.requireNonNull(c);
	        boolean modified = false;
	        Iterator<K> it = super.iterator();
	        while (it.hasNext()) {
	            if (!c.contains(it.next())) {
	                it.remove();
	                modified = true;
	            }
	        }
	        return modified;
	    }
	
}
