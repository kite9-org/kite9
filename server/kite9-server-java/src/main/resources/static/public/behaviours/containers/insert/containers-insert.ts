import { hasLastSelected, getKite9Target, createUniqueId, changeId, onlyLastSelected } from '../../../bundles/api.js'
import { getMainSvg, getSVGCoords, getElementPageBBox, currentTarget } from '../../../bundles/screen.js'
import { getBefore } from '../../../bundles/ordering.js'
import { getElementUri, Palette } from '../../../classes/palette/palette.js';
import { PaletteSelector, Selector } from '../../../bundles/types.js';
import { Command } from '../../../classes/command/command.js';
import { Containment } from '../../../classes/containment/containment.js';
import { ContextMenu, ContextMenuCallback } from '../../../classes/context-menu/context-menu.js';

function defaultInsertSelector() {
	return Array.from(getMainSvg().querySelectorAll('[k9-contains].selected:not([k9-contains=""])')) as SVGGraphicsElement[];
}

function defaultInsertableSelector(palettePanel: Element) {
	return Array.from(palettePanel.querySelectorAll("[id][k9-elem]")) as SVGGraphicsElement[];
}

/**
 * Provides functionality so that when the user clicks on a 
 * palette element it is inserted into the document.
 */
export function initInsertContextMenuCallback(
	palette: Palette,
	command: Command,
	containment: Containment,
	insertableSelector?: PaletteSelector,
	insertSelector?: Selector) : ContextMenuCallback {

	if (insertableSelector == undefined) {
		insertableSelector = defaultInsertableSelector;
	}

	if (insertSelector == undefined) {
		insertSelector = defaultInsertSelector;
	}

	/**
	 * Provides a contain option for the context menu
	 */
	return function(event: Event, contextMenu: ContextMenu) {

		const selectedElements = hasLastSelected(insertSelector());
		const lastSelectedElement = onlyLastSelected(insertSelector());

		// this is the palette element we are going to insert into them
		const palettePanel = palette.getOpenPanel();
		const droppingElement = onlyLastSelected(insertableSelector(palettePanel));

		function createInsertStep(e, drop, newId, beforeId) {
			return {
				"type": 'InsertUrl',
				"fragmentId": e.getAttribute('id'),
				"uriStr": getElementUri(drop, palettePanel),
				"beforeId": beforeId,
				"newId": newId
			}
		}

		function handleInsert(paletteElement: SVGGraphicsElement, selectedElements : Element[]) {
			const ownBBox = getElementPageBBox(paletteElement);
			Array.from(selectedElements)
				.filter(e => containment.canContainAll(paletteElement, e))
				.map(e => {
					const newId = createUniqueId();
					e.classList.remove("selected");
					const parentBBox = getElementPageBBox(e);

					// create the new svg element
					const clone = paletteElement.cloneNode(true) as SVGGraphicsElement;
					const pId = paletteElement.getAttribute("id");
					changeId(clone, pId, newId);

					// insert it in the correct place in the svg.
					const before = getBefore(e, palette.getOpenEvent(), []);
					if (before == undefined) {
						e.appendChild(clone)
					} else {
						before.parentElement.insertBefore(clone, before);
					}

					if (e == getKite9Target(currentTarget(palette.getOpenEvent()))) {
						// set the position so it appears at the location of the pointer event
						const coords = getSVGCoords(palette.getOpenEvent());
						clone.style.transform = "translateX(" + (coords.x - parentBBox.x - (ownBBox.width / 2)) + "px) translateY(" + (coords.y - parentBBox.y - (ownBBox.height / 2)) + "px)";
					} else {
						// set so that it's in the middle of the container.
						clone.style.transform = "translateX(" + ((parentBBox.width / 2) - (ownBBox.width / 2)) + "px) translateY(" + ((parentBBox.height / 2) - (ownBBox.height / 2)) + "px)";
					}

					// update the adl
					return createInsertStep(e, paletteElement, newId,
						(before == undefined ? undefined : before.getAttribute("id")));
				})
				.forEach(c => command.push(c));
		}


		if (lastSelectedElement) {
			const allowed = containment.canContainAll(droppingElement, lastSelectedElement);
			if (allowed) {
				contextMenu.addControl(event, "/public/behaviours/containers/insert/insert.svg", "Insert",
					function() {
						contextMenu.destroy();
						handleInsert(droppingElement, selectedElements);
						palette.destroy();
						command.perform();
						event.stopPropagation();
					});
			}
		}
	}
}


