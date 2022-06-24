import { hasLastSelected, getParentElement, parseInfo } from '/public/bundles/api.js';


export function initSelectContextMenuCallback(selector) {
	
	if (selector == undefined) {
		selector = function() {
			return document.querySelectorAll("[id][k9-ui~='grid'].selected")
		}
	}
	
	function performSelect(cm, event, horiz, elements) {
		
		function intersects(r1, r2) {
			const startIn = (r1[0] >= r2[0]) && (r1[0] < r2[1]);
			const endIn = (r1[1] > r2[0]) && (r1[1] <= r2[1]);
			return startIn || endIn;
		}
		
		elements.forEach(e => {
			const info = parseInfo(e);
			const range = horiz ? info['grid-y'] : info['grid-x'];
			const container = e.parentElement;
			
			Array.from(container.children).forEach(f => {
				const details = parseInfo(f);
				if ((details != null) && details['grid-x']) {
					const intersect = horiz ? intersects(details['grid-y'], range) :
						intersects(details['grid-x'], range);
				
					if (intersect) { //&& (!f.classList.contains('grid-temporary'))) {
						f.classList.add("selected");
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