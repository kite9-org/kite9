import { hasLastSelected } from '../../../bundles/api.js'
import { getMainSvg } from '../../../bundles/screen.js'
import { Finder, PaletteSelector, Selector } from '../../../bundles/types.js';
import { ContextMenu, ContextMenuCallback } from '../../../classes/context-menu/context-menu.js';
import { Palette, PaletteLoadCallback, SelectorFunction } from '../../../classes/palette/palette.js';

/**
 * Provides the palette-menu option for the context menu on the main diagram.
 */
export function initPaletteContextMenuCallback(
	palette: Palette,
	selector: Selector = undefined,
	paletteSelector: SelectorFunction = undefined): ContextMenuCallback {

	if (selector == undefined) {
		selector = function() {
			return Array.from(getMainSvg().querySelectorAll("[id].selected"));
		}
	}

	if (paletteSelector == undefined) {
		paletteSelector = function(e) {
			return e != undefined;  // all palettes shown and all elements active
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
				function() {
					contextMenu.destroy();
					palette.open(event,
						paletteSelector,
					);
				});
		}
	}
}


/**
 * Given a URI, returns the element itself, which we can use as the template
 */
export function initPaletteFinder(): Finder {

	return function(uri: string): Element {
		const options = Array.from(document.querySelectorAll("div.palette-item"))
			.filter(pDiv => uri.startsWith(pDiv.getAttribute("k9-palette-uri")))
			.map(pDiv => {
				const paletteId = pDiv.getAttribute("id");
				const elementId = uri.substring(uri.lastIndexOf("#") + 1) + paletteId;
				return pDiv.querySelector('#' + elementId);
			});

		return options[0];
	}
}


/**
 * Allows elements on the palette to open up a context menu when clicked.
 */
export function initMenuPaletteCallback(
	paletteContextMenu: ContextMenu,
	menuChoiceSelector: PaletteSelector = undefined): PaletteLoadCallback {

	if (menuChoiceSelector == undefined) {
		menuChoiceSelector = function(palettePanel) {
			return Array.from(palettePanel.querySelectorAll("[id]"));
		}
	}

	return function(palette, palettePanel) {
		function click(event: Event) {
			paletteContextMenu.destroy();
			paletteContextMenu.handle(event);
			event.stopPropagation();
		}

		menuChoiceSelector(palettePanel).forEach(function(v) {
			v.removeEventListener("click", click);
			v.addEventListener("click", click);
		})
	}


}
