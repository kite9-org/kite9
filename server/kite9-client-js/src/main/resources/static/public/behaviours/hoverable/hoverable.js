import { getMainSvg, getHtmlCoords, currentTarget } from '/public/bundles/screen.js';
import { getKite9Target } from '/public/bundles/api.js';


/**
 * Adds hoverable behaviour
 */
export function initHoverable(selector, allowed) {
	
	var selectedElement;
	
	if (allowed == undefined) {
		allowed = function(v) {
			return v!=undefined;
		}
	}
	
	function mouseover(event) {
		if (event.handledHover) {
			return;
		}
		
		var v = getKite9Target(currentTarget(event));
		
		if ((v != null) && (allowed(v)) && (v != selectedElement)) {
			var classes = v.classList;
			if (!classes.contains("mouseover")) {
				classes.add("mouseover");
				
				if (selectedElement != undefined) {
					selectedElement.classList.remove("mouseover")
				}

				selectedElement = v;

				// prevents parent elements from highlighting too.
				event.handledHover = true;
			}
		}
	}

	function mouseout(event) {
		var v = currentTarget(event);
		if (allowed(v)) {
			var classes = v.classList;
			classes.remove("mouseover");
			if (selectedElement == v) {
				selectedElement = undefined;
			}
		}
	}
	
	if (selector == undefined) {
		selector = function() { return getMainSvg().querySelectorAll("[id][k9-elem]"); }
	}
	
	window.addEventListener('DOMContentLoaded', function() {
		selector().forEach(function(v) {
	    	v.removeEventListener("mouseover", mouseover);
	    	v.addEventListener("mouseover", mouseover);

	    	v.removeEventListener("mouseout", mouseout);
	    	v.addEventListener("mouseout", mouseout);

	    	v.removeEventListener("touchmove", mouseover);
	    	v.addEventListener("touchmove", mouseover, { passive: false } );
	    	
		})
	})
}





