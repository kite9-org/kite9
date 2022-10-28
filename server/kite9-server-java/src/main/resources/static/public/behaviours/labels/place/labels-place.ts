import { getMainSvg } from '../../../bundles/screen.js'
import { parseStyle } from '../../../bundles/css.js'
import { hasLastSelected, parseInfo, getContainingDiagram, reverseDirection, createUniqueId } from '../../../bundles/api.js'

const PLACEMENTS = [ "top-left", "top", "top-right", "left", "none", "right", "bottom-left", "bottom", "bottom-right" ];
const STYLE_NAME = '--kite9-label-placement'

function getPlacement(command, e) {
	const adlElement = command.getADLDom(e.getAttribute("id"));
	var style = parseStyle(adlElement.getAttribute("style"));
	var l = style[STYLE_NAME];
	return l;
}

function drawPlacement(event, cm, placement, selected) {
	if (placement == null) {
		placement = "none";
	}
	
	var out = cm.addControl(event, "/public/behaviours/labels/place/" + placement.toLowerCase().replace("_","-") + ".svg",
			 "placement (" + placement + ")",
			 undefined);
	
	var img = out.children[0];

	if (selected == placement) {
		img.setAttribute("class", "selected");
	}

	return img;
}


export function initPlaceLabelContextMenuCallback(placementProperty, command, selector) {
	
	if (selector == undefined) {
		selector = function () {
			return getMainSvg().querySelectorAll("[id][k9-ui~=place].selected");
		}
	}
	
	/**
	 * Provides a placement option for the context menu
	 */
	return function (event, contextMenu) {

		const e = hasLastSelected(selector());

		if (e.length> 0) {
			const ls = onlyLastSelected(e);
			const placement = getPlacement(command, ls);
			var img = drawPlacement(event, contextMenu, placement);

			img.addEventListener("click", formEvent => {
				contextMenu.clear();
				placementProperty.populateForm(formEvent, contextMenu, e);
			});
		}
	};	
}

export function initLabelPlacementPropertyFormCallback(command) {
	
	return function(propertyOwner, contextEvent, contextMenu, selectedElements) {
		const ls = onlyLastSelected(selectedElements);
		const placement = getPlacement(command, ls);
	
		PLACEMENTS.forEach(s => {
			var img2 = drawPlacement(event, contextMenu, s, placement);
			if (placement != s) {
				img2.setAttribute("title", s);
				img2.addEventListener("click", (formEvent) => propertyOwner.setProperty(contextEvent, formEvent, contextMenu, selectedElements));
			} 
		});
	}	
}

export function initLabelPlacementPropertySetCallback(command) {
	
	return function(propertyOwner, contextEvent, formEvent, contextMenu, selectedElements) {
		
		const placement = formEvent.currentTarget.getAttribute("title");
		selectedElements.forEach(e => {
			
			const existing = getPlacement(command, e);
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

