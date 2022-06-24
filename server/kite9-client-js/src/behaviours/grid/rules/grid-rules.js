import { parseInfo, getParentElement, isConnected, isDiagram, isGrid, getContainerChildren, getNextSiblingId, } from '/public/bundles/api.js';
import { getMainSvg } from '/public/bundles/screen.js';

export function initGridContainmentCallback() {
	
	function paletteIsCell(e) {
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
	return function(elements, parents, children) {
		parents = Array.isArray(parents) ? parents: Array.from(parents);
		
		const grids = parents.filter(p => isGrid(p)); 

		if (grids.length > 0) {
			elements = Array.isArray(elements) ? elements: Array.from(elements);
			const cells = elements.filter(e => e == "*" || paletteIsCell(e));
			return cells;
		}

		return [];
	}
}