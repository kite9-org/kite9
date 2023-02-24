import { isGridLayout } from '../common-grid.js'
import { ContainsCallback, ContainmentRuleCallback, intersects } from '../../../classes/containment/containment.js';

const CELL_LABEL = 'cell';
const ALLOWED_GRID_CONTENTS = new Set(['terminator', 'port', 'label']);

export function initGridContainsCallback() : ContainsCallback {

	return function(e: Element) {
		
		if (isGridLayout(e)) {
			return [CELL_LABEL];
		} else {		
			return [];
		}
	}
}



export function initGridContainmentRuleCallback() : ContainmentRuleCallback {
	
	return function(elementTypes, parentContainsTypes) {
		if (intersects(elementTypes, ALLOWED_GRID_CONTENTS)) {
			// these get a pass
			return true;
		}
		
		if (parentContainsTypes.has(CELL_LABEL)) {
			// in this case, we are in 'grid mode', so the element should 
			// be a cell.
			return elementTypes.has(CELL_LABEL);
		}
		
		return true;
	}
}