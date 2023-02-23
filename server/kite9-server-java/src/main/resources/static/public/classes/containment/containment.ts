
/**
 * For a given element, return it's type
 */
export type TypeCallback = (element: Element) => string

/**
 * For a given element, return a list of types of element it can contain
 */
export type ContainsCallback = (element: Element) => string[]

/**
 * Given an item with `elementTypes`, can it be contained in a container allowing `containsTypes`
 */
export type ContainmentRuleCallback = (elementTypes: Set<string>, containsTypes: Set<string>) => boolean

/** 
 * Handles drag and drop rules, as well as surround/contain and insert/replace. 
 */
export class Containment {

	containmentRuleCallbacks : ContainmentRuleCallback[] = []
	typeCallbacks : TypeCallback[] = []
	containsCallbacks : ContainsCallback[] = []
	
	addContainmentRuleCallback(cb: ContainmentRuleCallback) {
		this.containmentRuleCallbacks.push(cb);
	}
	
	addTypeCallback(cb: TypeCallback) {
		this.typeCallbacks.push(cb);
	}
	
	addContainsCallback(cb: ContainsCallback) {
		this.containsCallbacks.push(cb);
	}
	
	getTypes(element: Element) : Set<string> {
		return new Set(this.typeCallbacks
			.map(cb => cb(element)));
	}
	
	getContainsTypes(element: Element) : Set<string> {
		return new Set(this.containsCallbacks
			.flatMap(cb => cb(element)));
	}
	
	contains(elementTypes: Set<string>, containsTypes: Set<string>) : boolean {
		return this.containmentRuleCallbacks.map(cb => cb(elementTypes, containsTypes))
			.reduce((a, b) => a && b);		
	}
	
	
	/**
	 * This is the main function.  elements is the set of elements that will be reduced and returned.  
	 */
	allowed(elements : Element[], parent : Element) : Element[] {
		const parentTypes = this.getContainsTypes(parent);
	
		return elements.filter((e, i) => this.contains(this.getTypes(e), parentTypes));
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
	
	canReplace(newElement: Element, oldElement: Element) : boolean {
		const eqSet = (xs: Set<string>, ys: Set<string>) =>
			xs.size === ys.size &&
			[...xs].every((x) => ys.has(x));	
			
		return eqSet(this.getTypes(newElement), this.getTypes(oldElement));
	}

}