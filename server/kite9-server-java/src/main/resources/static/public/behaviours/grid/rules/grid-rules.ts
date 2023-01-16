import { isGrid } from '../../../bundles/api.js'
import { ContainmentCallback } from '../../../classes/containment/containment.js';

export function initGridContainmentCallback() : ContainmentCallback {
	
	function paletteIsCell(e: Element) : boolean {
		if (e == undefined) {
			return false;
		}
		
		const attrValue = e.getAttribute("k9-palette");
		if (attrValue) {
			return attrValue.split(" ").includes("cell");
		}
		
		if (e.classList.contains('grid-temporary')) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Overrides the elements if we're in grid mode.
	 */
	return function(elements, parent) {
		if (isGrid(parent)) {
			const cells = elements.filter(e => paletteIsCell(e));
			return cells;
		}

		return [];
	}
}