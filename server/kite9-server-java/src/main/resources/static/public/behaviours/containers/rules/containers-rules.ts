import { isGrid } from '../../../bundles/api.js'
import { ElementBiFilter } from '../../../bundles/types.js';
import { ContainmentCallback } from '../../../classes/containment/containment.js';

function getTypes(element: Element | null, attr: string)  : string[] {

	if (element == undefined) {
		return [];
	}
	
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

/**
 * Three attributes:
 * 
 * k9-palette:  For parents, elements and children, a list of types.
 * k9-contains:  For parents and elements, which types are allowed inside them.
 * k9-containers:  For children and elements, a list of which container types they allow.
 * 
 */
export function initAttributeContainmentCallback() : ContainmentCallback {
	
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

/**
 * Creates a filter whereby two elements must have a certain combination of k9-palette types.
 */
export function initBiFilter(e1Match: string[], e2Match: string[]) : ElementBiFilter {

	return function(e1, e2) {
		const e1Types = e1 ? getTypes(e1, 'k9-palette') : [];
		const e2Types = e2 ? getTypes(e2, 'k9-palette') : [];
		
		const e1Ok = intersectionRule(e1Match, e1Types).length > 0;
		const e2Ok = intersectionRule(e2Match, e2Types).length > 0;
		
		return e1Ok && e2Ok;	
	}
}

