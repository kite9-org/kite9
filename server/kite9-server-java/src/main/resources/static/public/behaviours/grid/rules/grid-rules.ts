import { isGrid } from '../../../bundles/api.js'
import { TypeCallback, ContainsCallback, ContainmentRuleCallback } from '../../../classes/containment/containment.js';

const CELL_LABEL = 'cell';

export function initGridCellTemporaryTypeCallback() : TypeCallback {
	
	return function(e: Element) {
		if (e.classList.contains('grid-temporary')) {
			return CELL_LABEL;
		} else {
			return null;
		}
	}
}

export function initGridContainsCallback() : ContainsCallback {

	return function(e: Element) {
		
		if (isGrid(e)) {
			return [CELL_LABEL];
		} else {
			return [];
		}
	}
}



export function initGridContainmentRuleCallback() : ContainmentRuleCallback {
	
	return function(elementTypes, parentContainsTypes) {
		if (parentContainsTypes.has(CELL_LABEL)) {
			return elementTypes.has(CELL_LABEL);
		} else {
			return true;
		}
	}
}