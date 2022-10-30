import { getMainSvg } from '../../../bundles/screen.js'
import { parseStyle } from '../../../bundles/css.js'
import { hasLastSelected, onlyLastSelected } from '../../../bundles/api.js'
import { Command } from '../../../classes/command/command.js';
import { ContextMenu, ContextMenuCallback } from '../../../classes/context-menu/context-menu.js';
import { FormCallback, Property, SetCallback } from '../../../classes/context-menu/property.js';
import { Selector } from '../../../bundles/types.js';

const PLACEMENTS = ["top-left", "top", "top-right", "left", "none", "right", "bottom-left", "bottom", "bottom-right"];
const STYLE_NAME = '--kite9-label-placement'

function getPlacement(command: Command, e: Element) {
	const adlElement = command.getADLDom(e.getAttribute("id"));
	const style = parseStyle(adlElement.getAttribute("style"));
	const l = style[STYLE_NAME];
	return l;
}

function drawPlacement(event: Event, cm: ContextMenu, placement: string, selected ='') {
	if (placement == null) {
		placement = "none";
	}

	const out = cm.addControl(event, "/public/behaviours/labels/place/" + placement.toLowerCase().replace("_", "-") + ".svg",
		"placement (" + placement + ")",
		undefined);

	const img = out.children[0];

	if (selected == placement) {
		img.setAttribute("class", "selected");
	}

	return img;
}


export function initPlaceLabelContextMenuCallback(
	placementProperty: Property, 
	command: Command, 
	selector: Selector) : ContextMenuCallback {

	if (selector == undefined) {
		selector = function() {
			return Array.from(getMainSvg().querySelectorAll("[id][k9-ui~=place].selected"));
		}
	}

	/**
	 * Provides a placement option for the context menu
	 */
	return function(event: Event, contextMenu: ContextMenu) {

		const e = hasLastSelected(selector());

		if (e.length > 0) {
			const ls = onlyLastSelected(e);
			const placement = getPlacement(command, ls);
			const img = drawPlacement(event, contextMenu, placement);

			img.addEventListener("click", formEvent => {
				contextMenu.clear();
				placementProperty.populateForm(formEvent, contextMenu, e);
			});
		}
	};
}

export function initLabelPlacementPropertyFormCallback(command: Command): FormCallback {

	return function(propertyOwner, contextEvent, contextMenu, selectedElements) {
		const ls = onlyLastSelected(selectedElements);
		const placement = getPlacement(command, ls);

		PLACEMENTS.forEach(s => {
			const img2 = drawPlacement(event, contextMenu, s, placement);
			if (placement != s) {
				img2.setAttribute("title", s);
				img2.addEventListener("click", (formEvent) => propertyOwner.setProperty(contextEvent, formEvent, contextMenu, selectedElements));
			}
		});
	}
}

export function initLabelPlacementPropertySetCallback(command: Command): SetCallback {

	return function(propertyOwner, contextEvent, formEvent, contextMenu, selectedElements) {

		const placement = (formEvent.currentTarget as Element).getAttribute("title");
		selectedElements.forEach(e => {

			const id = e.getAttribute("id");

			if (PLACEMENTS.includes(placement)) {
				command.push({
					fragmentId: id,
					type: 'ReplaceStyle',
					name: STYLE_NAME,
					to: placement,
					from: getPlacement(command, e)
				});
			}
		});

	}
}

