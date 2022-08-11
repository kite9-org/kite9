import { hasLastSelected, getParentElements } from '/public/bundles/api.js'
import { getMainSvg } from '/public/bundles/screen.js'

/**
 * Provides the palette-menu option for the context menu on the main diagram.
 */
export function initPaletteContextMenuCallback(palette, selector, paletteSelector) {
	
	if (selector == undefined) {
		selector = function() {
			return getMainSvg().querySelectorAll("[id].selected");
		}
	}
	
	if (paletteSelector == undefined) {
		paletteSelector = function(e) {
			return e!=undefined;  // all palettes shown and all elements active
		}
	}
	
	/**
	 * Provides a link option for the context menu
	 */
	return function(event, contextMenu) {
		
		const selectedElements = hasLastSelected(selector());

		if (selectedElements.length > 0) {			
			contextMenu.addControl(event, "/public/behaviours/palettes/menu/open-palette.svg",
				"Open Palette", 
				function(e2, selector) {
					contextMenu.destroy();
					palette.open(event,
						paletteSelector,
						"menu"
					);
				});
		}
	}
}

/**
 * Allows elements on the palette to open up a context menu when clicked.
 */
export function initMenuPaletteCallback(paletteContextMenu, menuChoiceSelector) {
	
	if (menuChoiceSelector == undefined) {
		menuChoiceSelector = function(palettePanel) {
			return palettePanel.querySelectorAll("[id][k9-palette]");
		}
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
