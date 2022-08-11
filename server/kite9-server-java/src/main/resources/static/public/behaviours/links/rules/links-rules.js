import { isDiagram, isLink, isLabel, isTerminator } from '/public/bundles/api.js';


/**
 * Link elements can only go in the diagram element, they can only contain four children:
 *  - to
 *  - from
 *  - fromLabel
 *  - toLabel
 */
export function initLinkContainmentCallback() {
	
	return function(elements, parents, children) {
		const okParents = parents.filter(e => isDiagram(e));
		
		if (okParents.length == 0) {
			return [];
		}
			
		return elements.filter(e => isLink(e));
	}
	
}	

export function initTerminatorContainmentCallback() {
	
	return function(elements, parents, children) {
		const okParents = parents.filter(e => isLink(e));
		
		if (okParents.length == 0) {
			return [];
		}
		
		return elements.filter(e => isTerminator(e));
	}
}

export function initLabelContainmentCallback() {
	
	return function(elements, parents, children) {
		const okParents = parents.filter(e => isLink(e));
		
		if (okParents.length == 0) {
			return [];
		}
		
		return elements.filter(e => isLabel(e));
	}
	
}