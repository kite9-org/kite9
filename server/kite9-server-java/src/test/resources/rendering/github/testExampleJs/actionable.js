import { getMainSvg } from '/github/kite9-org/kite9/client/bundles/screen.js?v=v0.5'

/**
 * Allows the context menu to appear when the user clicks an element with an id
 */    
export function initActionable(contextMenu, selector) {

	/**
	 * Displays a context menu when the user clicks on an element.
	 */
	function click(event) {
		contextMenu.destroy();
		
		if (getMainSvg().style.cursor =='wait') {
			return;
		}
		
		contextMenu.handle(event);
		event.stopPropagation();
	}
	
	if (selector == undefined) {
		selector = function() {
			return getMainSvg().querySelectorAll("[id]");
		}
	}

	window.addEventListener('DOMContentLoaded', function() {
		
		selector().forEach(function(v) {
	    	// set up listeners
	    	v.removeEventListener("click", click);
	    	v.addEventListener("click", click);
	    })
	});

}