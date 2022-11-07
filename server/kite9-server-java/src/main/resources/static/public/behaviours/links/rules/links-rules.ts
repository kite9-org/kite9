import { isDiagram, isLink, isLabel, isTerminator } from '../../../bundles/api.js';
import { ContainmentCallback, WILDCARD } from '../../../classes/containment/containment.js';


export function initTerminatorContainmentCallback() : ContainmentCallback {
	
	return function(elements, parents, children) {
		const okParents = parents.filter(e => (e == WILDCARD) || isLink(e));
		
		if (okParents.length == 0) {
			return [];
		}
		
		return elements.filter(e => isTerminator(e));
	}
}

export function initLabelContainmentCallback() : ContainmentCallback {
	
	return function(elements, parents, children) {
		const okParents = parents.filter(e => isLink(e));
		
		if (okParents.length == 0) {
			return [];
		}
		
		return elements.filter(e => isLabel(e));
	}
	
}