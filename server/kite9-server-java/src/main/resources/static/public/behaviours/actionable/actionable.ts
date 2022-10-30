import { getMainSvg } from '../../bundles/screen.js'
import { ContextMenu } from '../../classes/context-menu/context-menu.js'
import { Selector } from '../../bundles/types.js'

/**
 * Allows the context menu to appear when the user clicks an element with an id
 */    
export function initActionable(contextMenu : ContextMenu, selector : Selector = undefined) {

	/**
	 * Displays a context menu when the user clicks on an element.
	 */
	function click(event : Event) {
		contextMenu.destroy();
		
		if (getMainSvg().style.cursor =='wait') {
			return;
		}
		
		contextMenu.handle(event);
		event.stopPropagation();
	}
	
	if (selector == undefined) {
		selector = function() {
			return Array.from(getMainSvg().querySelectorAll("[id]"));
		}
	}

	window.addEventListener('DOMContentLoaded', function() {

		selector().forEach(function(v : Element) {
			// set up listeners
			v.removeEventListener("click", click);
			v.addEventListener("click", click);
		})
	});

}