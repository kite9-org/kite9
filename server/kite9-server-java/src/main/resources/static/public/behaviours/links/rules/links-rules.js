import { isDiagram, isLink, isLabel, isTerminator } from '/public/bundles/api.js';


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