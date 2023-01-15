import { isGrid } from '../../../bundles/api.js'
import { ContainmentCallback, WcElement } from '../../../classes/containment/containment.js';

/**
 * Three attributes:
 * 
 * k9-palette:  For parents, elements and children, a list of types.
 * k9-contains:  For parents and elements, which types are allowed inside them.
 * k9-containers:  For children and elements, a list of which container types they allow.
 * 
 */
export function initAttributeContainmentCallback() : ContainmentCallback {
	
	function regularLayoutOnly(parents: WcElement[]) : WcElement[] {
		return Array.from(parents)
			.filter(p => p instanceof Element ? !isGrid(p) : p)
	}
	
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
	
	function getTypes(element: WcElement, attr: string)  : string[] {
		if (element == '*') {
			return [ '*' ]; 	// wildcard
		} else if (element instanceof Element) {
			const attrValue = element.getAttribute(attr);
			if (attrValue) {
				return attrValue.split(" ") || [];
			}
		}

		if (attr == 'k9-containers') {
			return [ "*" ];	// wildcard
		} else {
			return [ ];	// nothing
		}
	}
	
	function getTypesIntersection(elements: WcElement[], attr : string) : string[] {
		const out = elements
			.map(e => getTypes(e, attr))
			.reduce((a, b) => intersectionRule(a, b), ['*']);
		return out;
	}
	
	return function(elements: Element[], p: WcElement[], children: WcElement[]) {
		const parents = regularLayoutOnly(p)
		
		const parentBoundsOnElement = parents.length > 0 ? getTypesIntersection(parents, 'k9-contains') : [];
		const childBoundsOnElement = children.length > 0 ? getTypesIntersection(children, 'k9-containers') : [];
				
		const out = elements.filter(e => {
			let elementTypes = getTypes(e, 'k9-palette');
			
			if (parentBoundsOnElement.length > 0) {
				elementTypes = intersectionRule(parentBoundsOnElement, elementTypes);
			}
			
			if (childBoundsOnElement.length > 0) {
				elementTypes = intersectionRule(childBoundsOnElement, elementTypes);
			}
			
			console.log(`Parent Bounds: ${parentBoundsOnElement}, Child Bounds: ${childBoundsOnElement}, Element Types: ${elementTypes}`)
			
			return elementTypes.length > 0;
		});
		
		return out;
	}

}



