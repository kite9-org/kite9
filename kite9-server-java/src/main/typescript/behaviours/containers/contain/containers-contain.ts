import { hasLastSelected, onlyLastSelected, createUniqueId, getParentElement, getNextSiblingId } from '../../../bundles/api.js'
import { getMainSvg } from '../../../bundles/screen.js'
import { Selector } from '../../../bundles/types.js';
import { Command } from '../../../classes/command/command.js';
import { Containment } from '../../../classes/containment/containment.js';
import { ContextMenu, ContextMenuCallback } from '../../../classes/context-menu/context-menu.js';
import { getElementUri, Palette } from '../../../classes/palette/palette.js';


const defaultContainSelector: Selector = function() {
	return Array.from(getMainSvg().querySelectorAll("[id][k9-elem].selected"));
}

/**
 * Provides functionality so that when the user clicks on a 
 * palette element it is inserted into the document.
 */
export function initContainContextMenuCallback(
	palette: Palette, 
	command: Command, 
	containment: Containment, 
	containSelector: Selector = undefined) :ContextMenuCallback {

	if (containSelector == undefined) {
		containSelector = defaultContainSelector;
	}

	/**
	 * Creates the palette-element inside the container of the lastSelected element.
	 * Moves all the on-diagram selected elements inside it  
	 */
	return function(event: Event, contextMenu: ContextMenu) {

		const selectedElements = hasLastSelected(containSelector());
		const lastSelectedElement = onlyLastSelected(containSelector());

		// this is the palette element we are going to contain them with
		const droppingElement = palette.get().querySelector("[id].lastSelected");
		const palettePanel = palette.getOpenPanel();

		function createInsertStep(e: Element, drop: Element, newId: string) {
			return {
				"type": 'InsertUrl',
				"uriStr": getElementUri(drop, palettePanel),
				"fragmentId": getParentElement(e).getAttribute("id"),
				"beforeId": e.getAttribute("id"),
				"newId": newId,
				"deep": true
			}
		}

		function createContainStep(e: Element, toId: string) {
			return {
				"type": 'Move',
				"to": toId,
				"from": getParentElement(e).getAttribute("id"),
				"fromBefore": getNextSiblingId(e),
				"moveId": e.getAttribute('id'),
			}
		}

		if (lastSelectedElement) {
			const parentElement = getParentElement(lastSelectedElement);
			const allowed = containment.canContainAny(droppingElement, parentElement);
			const newId = createUniqueId();

			if (allowed) {
				contextMenu.addControl(event, "/public/behaviours/containers/contain/contain.svg", "Contain",
					function() {
						contextMenu.destroy();
						// create the container element
						command.push(createInsertStep(lastSelectedElement, droppingElement, newId));

						// now move everything else into it
						Array.from(selectedElements)
							.filter(e => containment.canContainAll(e, droppingElement))
							.forEach(e => command.push(createContainStep(e, newId)));

						palette.destroy();
						command.perform();
						event.stopPropagation();
					});
			}
		}

	}
}



