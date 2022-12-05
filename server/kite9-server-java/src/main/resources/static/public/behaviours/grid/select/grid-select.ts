import { hasLastSelected, parseInfo } from '../../../bundles/api.js'
import { getMainSvg } from '../../../bundles/screen.js';
import { Selector } from '../../../bundles/types.js';
import { ContextMenu, ContextMenuCallback } from '../../../classes/context-menu/context-menu.js';


export function initSelectContextMenuCallback(selector: Selector = undefined) : ContextMenuCallback {
	
	if (selector == undefined) {
		selector = function() {
			return Array.from(getMainSvg().querySelectorAll("[id][k9-ui~='grid'].selected"))
		}
	}
	
	function performSelect(_cm: ContextMenu, _event: Event, horiz: boolean, elements: Element[]) {
		
		function intersects(r1 : number, r2: number) {
			const startIn = (r1[0] >= r2[0]) && (r1[0] < r2[1]);
			const endIn = (r1[1] > r2[0]) && (r1[1] <= r2[1]);
			return startIn || endIn;
		}
		
		elements.forEach(e => {
			const info = parseInfo(e);
			const range = horiz ? info['grid-y'] : info['grid-x'];
			const container = e.parentElement;
			
			Array.from(container.children).forEach(f => {
				const details = parseInfo(f as Element);
				if ((details != null) && details['grid-x']) {
					const intersect = horiz ? intersects(details['grid-y'], range) :
						intersects(details['grid-x'], range);
				
					if (intersect) { //&& (!f.classList.contains('grid-temporary'))) {
						(f as Element).classList.add("selected");
					}
				}
			});
		});
	}
	
	/**
	 * Provides overlays for selecting rows, columns
	 */
	return function(event, cm) {
		
		const e = hasLastSelected(selector());
		
		if (e.length > 0) {
			cm.addControl(event, "/public/behaviours/grid/select/vertical.svg",  "Select Column", () => performSelect(cm, event, false, selector()));
			cm.addControl(event, "/public/behaviours/grid/select/horizontal.svg",  "Select Row", () => performSelect(cm, event, true, selector()));
		}
	}
	
	
}