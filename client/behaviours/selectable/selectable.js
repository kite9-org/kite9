import { getMainSvg, currentTarget } from '/github/kite9-org/kite9/client/bundles/screen.js?v=v0.5'
import { getKite9Target } from '/github/kite9-org/kite9/client/bundles/api.js?v=v0.5'


// Adds .selected class when the user mouseups over an element.
// Adds .lastSelected class to a single element, which is the last one clicked on

export function initSelectable(selector, singleSelect) {
	
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
				getMainSvg().querySelectorAll(".selected").forEach(c => {
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
		
		document.querySelectorAll(".lastSelected").forEach(c => {
			c.classList.remove("lastSelected")
		})
		
		classes.add("lastSelected")
		
		event.handledSelect = true;
	}
	
	if (selector == undefined) {
		selector = function() {
			return getMainSvg().querySelectorAll("[id]");
		}
	}
	
	getMainSvg().addEventListener("mousedown", mouseup);

}



