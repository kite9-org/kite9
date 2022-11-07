import { isLink, isLabel, isTerminator } from '../../../bundles/api.js';
import { ContainmentCallback, WILDCARD } from '../../../classes/containment/containment.js';


export function initTerminatorContainmentCallback() : ContainmentCallback {
	
	return function(elements, parents) {
		const okParents = parents.filter(e => (e == WILDCARD) || isLink(e));
		
		if (okParents.length == 0) {
			return [];
		}
		
		return elements.filter(e => e instanceof Element ? isTerminator(e) : false);
	}
}

export function initLabelContainmentCallback() : ContainmentCallback {
	
	return function(elements, parents) {
		const okParents = parents.filter(e => e instanceof Element ? isLink(e) : e);
		
		if (okParents.length == 0) {
			return [];
		}
		
		return elements.filter(e => isLabel(e));
	}
	
}