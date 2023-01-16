export type ContainmentCallback = (elements: Element[], parent: Element) => Element[]

/** 
 * Handles drag and drop rules, as well as surround/contain and insert.  Replace is handled elsewhere.
 */
export class Containment {

	callbacks : ContainmentCallback[] = []
	
	add(cb: ContainmentCallback) {
		this.callbacks.push(cb);
	}
	
	
	/**
	 * This is the main function.  elements is the set of elements that will be reduced and returned.  
	 */
	allowed(elements : Element[], parent : Element) : Element[] {
		// run each callback.  return the union of allowed elements from all callbacks.
		
		return this.callbacks
			.map(cb => cb(elements, parent))
			.reduce((a, b) => [...new Set([...a, ...b])]);
	}

	/**
	 * Helper function
	 */
	canContainAny(element: Element[] | Element, parent: Element) : boolean {
		return this.allowed(
			Array.isArray(element) ? element : [element], 
			parent).length == 1;
	}
	
	/**
	 * Helper function
	 */
	canContainAll(element: Element[] | Element, parent: Element) : boolean {
		const needed = Array.isArray(element) ? element.length : 1;
		return this.allowed(
			Array.isArray(element) ? element : [element], 
			parent
			).length == needed;
	}

}