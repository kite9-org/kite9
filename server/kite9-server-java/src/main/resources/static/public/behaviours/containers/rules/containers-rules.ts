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
	
	function layoutIsOk(parents: Element[]) {
		return Array.from(parents)
			.map(p => !isGrid(p))
			.reduce((a, b) => a && b, true);
	}
	
	function intersectionRule(set1, set2) {
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
	
	function getTypes(element, attr) {
		if (element == '*') {
			return [ '*' ]; 	// wildcard
		} else if (element != undefined) {
			const attrValue = element.getAttribute(attr);
			if (attrValue) {
				return attrValue.match(/\S+/g) || [];
			}
		}

		if (attr == 'k9-containers') {
			return [ "*" ];	// wildcard
		} else {
			return [ ];	// nothing
		}
	}
	
	function getTypesIntersection(elements, attr) {
		elements = Array.isArray(elements) ? elements : Array.from(elements);
		const out = elements
			.map(e => getTypes(e, attr))
			.reduce((a, b) => intersectionRule(a, b));
		return out;
	}
	
	return function(elements: Element[], parents: Element[], children: Element[]) {
		if (!layoutIsOk(parents)) {
			// containers-rules only works for general layouts.
			return [];
		}
		
		const parentBoundsOnElement = parents ? getTypesIntersection(parents, 'k9-contains') : undefined;
		const childBoundsOnElement = children ? getTypesIntersection(children, 'k9-containers') : undefined;
		
		elements = Array.isArray(elements) ? elements : Array.from(elements);
		
		const out = elements.filter(e => {
			let elementTypes = getTypes(e, 'k9-palette');
			
			if (parentBoundsOnElement) {
				elementTypes = intersectionRule(parentBoundsOnElement, elementTypes);
			}
			
			if (childBoundsOnElement) {
				elementTypes = intersectionRule(childBoundsOnElement, elementTypes);
			}
			
			return elementTypes.length > 0;
		});
		
		return out;
	}

}



