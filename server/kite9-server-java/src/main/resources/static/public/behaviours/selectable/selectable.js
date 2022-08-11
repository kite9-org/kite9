import { getMainSvg, currentTarget } from '/public/bundles/screen.js'
import { getKite9Target } from '/public/bundles/api.js'


export function clearSelectable(within) {
	within.querySelectorAll(".lastSelected").forEach(c => {
		c.classList.remove("lastSelected")
	})
}

// Adds .selected class when the user mouseups over an element.
// Adds .lastSelected class to a single element, which is the last one clicked on

export function initSelectable(selector, within, singleSelect) {
	
	if (within == undefined) {
		within = getMainSvg();
	}
	
	function mouseup(event) {
		if (event.handledSelect) {
			return;
		}
		
		var v = getKite9Target(currentTarget(event));
		
		if (v == undefined) {
			return;
		}
		
		var classes = v.classList;
		if (!classes.contains("selected")) {
			
			if (singleSelect) {
				// unselect all other elements
				within.querySelectorAll(".selected").forEach(c => {
					c.classList.remove("selected");
				})
				
				classes.add("selected");
			} else {
				classes.add("selected");

				// unselect nested elements
				v.querySelectorAll(".selected").forEach(c => {
					c.classList.remove("selected")
				})
				
				// unselect parent elements
				while (v) {
					v = v.parentElement;
					if (v != null) {
						v.classList.remove("selected")
					}
				}
			}
		} else {
			classes.remove("selected")
		}
		
		clearSelectable(within);

		classes.add("lastSelected")
		
		event.handledSelect = true;
	}
	
	if (selector == undefined) {
		selector = function() {
			return within.querySelectorAll("[id]");
		}
	}
	
	within.addEventListener("mousedown", mouseup);

}



