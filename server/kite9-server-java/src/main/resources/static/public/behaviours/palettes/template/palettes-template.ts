import { getDocumentParam, onlyLastSelected, setDocumentParam } from '../../../bundles/api.js'
import { Finder, PaletteSelector } from '../../../bundles/types.js';
import { ContextMenuCallback } from '../../../classes/context-menu/context-menu.js';
import { getElementUri, Palette, PaletteRevealCallback } from '../../../classes/palette/palette.js';


/**
 * Highlights default template items in the palettes.
 */
export function initPaletteUpdateDefaults(
	palette: Palette,
	linkFinder: Finder): PaletteRevealCallback {


	return function() {
		palette.get().querySelectorAll("[default]").forEach(e => e.removeAttribute("default"));
		const map = document['params'];

		for (const k in Object.keys(map)) {
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


/**
 * Allows the user to click to make this the default element
 */
export function initSetDefaultContextMenuCallback(
	palette: Palette,
	paramName: string,
	description: string,
	linkFinder: Finder,
	selector: PaletteSelector): ContextMenuCallback {

	/**
	 * Provides a contain option for the context menu
	 */
	return function(event, contextMenu) {
		const palettePanel = palette.getOpenPanel();
		const lastSelectedElement = onlyLastSelected(selector(palettePanel));

		if (lastSelectedElement) {
			const id = getElementUri(lastSelectedElement, palettePanel);
			const currentSelectionId = getDocumentParam(paramName);
			const alreadySelected = linkFinder(currentSelectionId);
			const active = lastSelectedElement == alreadySelected;
			const title = active ? "Default " + description : "Make default " + description;
			const ctrl = contextMenu.addControl(event, "/public/behaviours/palettes/template/default.svg", title,
				function() {
					contextMenu.destroy();
					setDocumentParam(paramName, id);
					initPaletteUpdateDefaults(palette, linkFinder);
					event.stopPropagation();
					palette.destroy();
				});

			const img = ctrl.children[0];

			if (active) {
				img.setAttribute("class", "selected");
			}
		}
	}
}