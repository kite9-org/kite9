import { isGrid } from '../../../bundles/api.js'
import { ContainmentCallback } from '../../../classes/containment/containment.js';

/**
 * Three attributes:
 * 
 * k9-palette:  For parents, elements and children, a list of types.
 * k9-contains:  For parents and elements, which types are allowed inside them.
 * k9-containers:  For children and elements, a list of which container types they allow.
 * 
 */
export function initAttributeContainmentCallback() : ContainmentCallback {
	
	function intersectionRule(set1: string[], set2: string[]) {
		if (set1.includes("*") && (set2.includes("*"))) {
			return [ ...set1, ...set2 ];
		} else if (set1.includes("*")) {
			return set2;
		} else if (set2.includes("*")) {
			return set1;
		} else {
			return set1.filter(e => set2.includes(e));
		}
	}
	
	function getTypes(element: Element, attr: string)  : string[] {
		const attrValue = element.getAttribute(attr);
		if (attrValue) {
			return attrValue.split(" ") || [];
		}

		if (attr == 'k9-containers') {
			return [ "*" ];	// wildcard
		} else {
			return [ ];	// nothing
		}
	}
	
	return function(elements: Element[], parent: Element) {
		if (isGrid(parent)) {
			// grid handled elsewhere
			return []
		}
				
		const parentBoundsOnElement = getTypes(parent, 'k9-contains');
				
		const out = elements.filter(e => {
			let elementTypes = getTypes(e, 'k9-palette');
			
			if (parentBoundsOnElement.length > 0) {
				elementTypes = intersectionRule(parentBoundsOnElement, elementTypes);
			}
			
			return elementTypes.length > 0;
		});
		
		return out;
	}

}



