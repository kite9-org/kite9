import { hasLastSelected } from '/public/bundles/api.js'
import { getMainSvg } from '/public/bundles/screen.js'

/**
 * k9-palette attribute says which type of element this is.  The element can be replaced with another element of the same type.
 */
function initDefaultMenuSelector() {
	return function() {
		return getMainSvg().querySelectorAll("[id].selected");
	}
}

function initDefaultMenuChoiceSelector() {
	return function(palettePanel) {
		return palettePanel.querySelectorAll("[id][k9-palette]");
	}
}

export function initPaletteContextMenuCallback(palette, selector) {
	
	if (selector == undefined) {
		selector = initDefaultMenuSelector();
	}
	
	/**
	 * Provides a link option for the context menu
	 */
	return function(event, contextMenu) {
		
		const selectedElements = hasLastSelected(selector());

		if (selectedElements.length > 0) {			
			contextMenu.addControl(event, "/public/behaviours/selectable/palette/open-palette.svg",
				"Open Palette", 
				function(e2, selector) {
					contextMenu.destroy();
					palette.open(event,
						e => true,
						"menu"
					);
				});
		}
	}
}

export function initMenuPaletteCallback(paletteContextMenu, selector, menuChoiceSelector) {
	
	if (selector == undefined) {
		selector = initDefaultMenuSelector();
	}
	
	if (menuChoiceSelector == undefined) {
		menuChoiceSelector = initDefaultMenuChoiceSelector();
	}
	
	return function(palette, palettePanel, type) {
		function click(event) {
			if (palette.getCurrentAction() == "menu") {
				paletteContextMenu.destroy();
				paletteContextMenu.handle(event);
				event.stopPropagation();
			}
		}
		
		menuChoiceSelector(palettePanel).forEach(function(v) {
	    	v.removeEventListener("click", click);
	    	v.addEventListener("click", click);
		})
	}
	
	
}
