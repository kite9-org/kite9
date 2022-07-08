import { getParentElement, getContainerChildren } from '/github/kite9-org/kite9/client/bundles/api.js?v=v0.2'

export const WILDCARD = "*";

export class Containment {
		
	constructor() {
		this.callbacks = [];
	}
	
	add(cb) {
		this.callbacks.push(cb);
	}
	
	
	/**
	 * This is the main function.  elements is the set of elements that will be reduced and returned.  
	 * Generally, these are SVG (kite9) elements, but you can also supply "*" ( a string-star), which acts as a wildcard.
	 * Parents and children are optional, and will "limit" the elements returned in the reduction.
	 */
	allowed(elements, parents, children) {
		// run each callback.  return the union of allowed elements from all callbacks.
		
		return this.callbacks
			.map(cb => cb(elements, parents, children))
			.reduce((a, b) => [...new Set([...a, ...b])]);
	}

	/**
	 * Helper function
	 */
	canContain(element, parent) {
		return this.allowed([element], Array.isArray(parent) ? parent : [parent]).length == 1;
	}
	
	/**
	 * Helper function
	 */
	canContainAll(elements, parent) {
		return this.allowed(elements, 
			Array.isArray(parent) ? parent : [parent]
			).length == elements.length;
	}
	
	canSurroundAll(elements, parents, children) {
		return this.allowed(elements, parents, children).length == elements.length;
	}
	
	/**
	 * Can insert into this container
	 */
	canInsert(containers, children) {
		return this.allowed([ WILDCARD ], containers, children).length == 1;
	}
}

