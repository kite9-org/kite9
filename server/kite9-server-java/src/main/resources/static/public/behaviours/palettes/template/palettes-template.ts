import { hasLastSelected } from '../../../bundles/api.js'
import { getElementUri } from '../../../classes/palette/palette.js';


/**
 * Highlights default template items in the palettes.
 */
export function initPaletteUpdateDefaults(palette, linkFinder) {
	
	
	return function() {
		palette.get().querySelectorAll("[default]").forEach(e => e.removeAttribute("default"));
		const map = document.params;
	
		for(var k in map) {
			if (Object.prototype.hasOwnProperty.call(map, k)) {
				if (k.endsWith("-template-uri")) {
					const v = map[k];
					const elem = linkFinder(v);
					if (elem) {
						elem.setAttribute("default", "true");
					}
				}
			}
		}
	}
}


/**
 * Allows the user to click to make this the default element
 */
export function initSetDefaultContextMenuCallback(palette, paramName, description, linkFinder, selector) {
	
    /**
	 * Provides a contain option for the context menu
	 */
	return function(event, contextMenu) {
		const palettePanel = palette.getOpenPanel();		
	    const lastSelectedElement = onlyLastSelected(selector(palettePanel));
		
		if (lastSelectedElement) {
			const id = getElementUri(lastSelectedElement, palettePanel);
			const currentSelectionId = document.params[paramName];
			const alreadySelected = linkFinder(currentSelectionId);
			const active = lastSelectedElement == alreadySelected;
			const title = active ? "Default "+description : "Make default "+description;
			const ctrl = contextMenu.addControl(event, "/public/behaviours/palettes/template/default.svg", title,
				function(e2, selector) {
					contextMenu.destroy();
					document.params[paramName] = id;
					initPaletteUpdateDefaults(palette, linkFinder);
					event.stopPropagation();
					palette.destroy();
				});
					
			var img = ctrl.children[0];
			
			if (active) {
				img.setAttribute("class", "selected");
			}			
		}
	}
}