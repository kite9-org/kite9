import { ElementBiFilter } from '../../../bundles/types.js';
import { Containment, ContainmentRuleCallback, ContainsCallback, TypeCallback } from '../../../classes/containment/containment.js';

export function initTypedRulesContainsCallback() : ContainsCallback {
	return (element) => {
		if (element == undefined) {
			return [];
		}
		
		const attrValue = element.getAttribute('k9-contains');
		if (attrValue) {
			return attrValue.split(" ") || [];
		}
	
		return [];
	}
}

export function initTypedRulesTypeCallback() : TypeCallback {
	return (element) => {
		if (element == undefined) {
			return '';
		}
		
		const attrValue = element.getAttribute('k9-type');
		return attrValue;
	}
}

function intersects(set1: Set<string>, set2: Set<string>): boolean {
	if (set1.has("*")) {
		return set2.size > 0;
	} else if (set2.has("*")) {
		return set1.size > 0;
	} else {
		for (const elem of set2) {
			if (set1.has(elem)) {
				return true;
			}
		}
	}
}

/**
 * Allows inclusion where any of the elementTypes is contained in the containsTypes (also allows wildcard * on either side)
 */
export function initTypedRulesContainmentRuleCallback() : ContainmentRuleCallback {
	
	return function(elementTypes: Set<string>, containsTypes: Set<string>) {
		return intersects(elementTypes, containsTypes);
	}

}

/**
 * Creates a filter whereby two elements must have a certain combination of element types.
 */
export function initBiFilter(containment: Containment, childAllowedTypes: string[], parentAllowedTypes: string[]) : ElementBiFilter {
	
	const childSet = new Set(childAllowedTypes);
	const parentSet = new Set(parentAllowedTypes);

	return function(e1, e2) {
		const e1Types = e1 ? containment.getTypes(e1) : new Set<string>();
		const e2Types = e2 ? containment.getTypes(e2) : new Set<string>();
		
		const e1Ok = intersects(e1Types, childSet);
		const e2Ok = intersects(e2Types, parentSet);
		
		return e1Ok && e2Ok;	
	}
}

