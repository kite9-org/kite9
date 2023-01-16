import { isLink, isLabel, isTerminator, getAffordances } from '../../../bundles/api.js';
import { ContainmentCallback } from '../../../classes/containment/containment.js';

/**
 * This says that a terminator can be dropped on any parent that has the 
 * connect behaviour set.
 */
export function initTerminatorContainmentCallback() : ContainmentCallback {
	
	return function(elements, parent) {
		if (!getAffordances(parent).includes('connect')) {
			return [];
		}
		
		const out = elements.filter(e => isTerminator(e));
		return out;
	}
}

/**
 * This says a link can contain a label?
 */
/*export function initLabelContainmentCallback() : ContainmentCallback {
	
	return function(elements, parent) {
		const okParents = parents.filter(e => e instanceof Element ? isLink(e) : e);
		
		if (okParents.length == 0) {
			return [];
		}
		
		return elements.filter(e => isLabel(e));
	}
	
}*/