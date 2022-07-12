import { getMainSvg } from '/github/kite9-org/kite9/client/bundles/screen.js?v=v0.6'
import { getExistingConnections, parseInfo, reverseDirection } from '/github/kite9-org/kite9/client/bundles/api.js?v=v0.6'

/**
 * Contains the functionality for linking drawing links between selected elements 
 * and a target.
 * 
 * There are some basic expectations about the way links work embedded in this behaviour:
 * 
 * 1.  drawDirection is an attribute on the link which indicates the direction it must go in (if any).  
 * 2.  <from> and <to> elements are present, and have reference="" to indicate where they link to.\
 * 3.  Labels are embedded within <from> and <to>.
 * 4.  Align links (links that the editor can remove) have the class "align".  
 * 
 * All of these are in any case defaults that can be overridden.
 */
export function initLinkable(linker, selector)  {
	
	function move(event) {
		linker.move(event);
	}
	
	function end(event) {
		linker.end(event);
	}
	
	if (selector == undefined) {
		selector = function() {
			return getMainSvg().querySelectorAll("[id][k9-elem]");
		}
	}

	window.addEventListener('DOMContentLoaded', function(event) {
		selector().forEach(function(v) {
			v.removeEventListener("mousemove", move);
			v.removeEventListener("touchmove", move);
			v.removeEventListener("mouseup", end);
			v.removeEventListener("touchend", end);
			
			v.addEventListener("mousemove", move);
			v.addEventListener("touchmove", move, { passive: false });
			v.addEventListener("mouseup", end);
			v.addEventListener("touchend", end);
		})
	})
}


export function initLinkerDropCallback(command, linker) {
	
	return function(dragState, event) {
		linker.end(event, false);  // this false means that changes don't get performed immediately
	}
	
}


/**
 * This is for updating links when they are being dragged.
 */
export function updateLink(e, from, to) {
	e.querySelectorAll("[k9-animate=link]").forEach(f => {
		f.setAttribute("d", "M"+from.x+" "+from.y+ "L"+to.x+" "+to.y);
	});
}

export function getAlignElementsAndDirections(id1, id2) {
	return getExistingConnections(id1, id2)
	 	.filter(e => e.getAttribute("k9-elem") == "align")
	 	.map(e => {
	 		const parsed = parseInfo(e);
	 		const d = parsed['direction'];
	 		const ids = parsed['link'];
	 		const reversed = ids[0] == id2;	
	 		return { 
	 			element: e,
	 			direction: reversed ? reverseDirection(d) : d
	 		}
	 	});
}
