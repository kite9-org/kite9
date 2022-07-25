import { hasLastSelected, createUniqueId, getParentElement, getNextSiblingId, getParentElements } from '/public/bundles/api.js'
import { getMainSvg } from '/public/bundles/screen.js'
import { getBeforeId } from '/public/bundles/ordering.js'


function defaultContainSelector() {
	return getMainSvg().querySelectorAll("[k9-palette].selected");
}

function defaultContainableSelector(palettePanel) {
	return palettePanel.querySelectorAll("[id][k9-palette]");	
}

/**
 * Provides functionality so that when the user clicks on a 
 * palette element it is inserted into the document.
 */
export function initContainPaletteCallback(command, containableSelector, containSelector) {
	
	if (containableSelector == undefined) {
		containableSelector = defaultContainableSelector;
	}
	
	if (containSelector == undefined) {
		containSelector = defaultContainSelector;
	}
	
	return function(palette, palettePanel) {
		
		function getElementUri(e) {
			var paletteId = palettePanel.getAttribute("id");
			var id = e.getAttribute("id");
			return palettePanel.getAttribute("k9-palette-uri")+"?format=adl#"+id.substring(0, id.length - paletteId.length);	
		}

		function createInsertStep(e, drop, newId) {
			return {
				"type": 'InsertUrl',
				"uriStr": getElementUri(drop),
				"fragmentId": getParentElement(e).getAttribute("id"),
				"beforeId" : e.getAttribute("id"),
				"newId": newId,
				"deep" : true
			}
		}
		
		function createContainStep(e, toId) {
			return {
				"type": 'Move',
				"to": toId,
				"from": getParentElement(e).getAttribute("id"),
				"fromBefore": getNextSiblingId(e),
				"moveId" : e.getAttribute('id'),
			}
		}
	
		function click(event) {
			if (palette.getCurrentAction() == 'contain') {
				// create the container element
				const droppingElement = palette.get().querySelector("[id].mouseover");
				const newId = createUniqueId();
				const selectedElements = containSelector();
				const lastElement = hasLastSelected(selectedElements, true);
				command.push(createInsertStep(lastElement, droppingElement, newId));
				
				// now move everything else into it
				Array.from(selectedElements).forEach(e => command.push(createContainStep(e, newId)));
				palette.destroy();		
				command.perform();
				event.stopPropagation();
			}
		}
	
		containableSelector(palettePanel).forEach(function(v) {
	    	v.removeEventListener("click", click);
	    	v.addEventListener("click", click);
		})
	}
}
	
/**
 * Adds contain option into context menu
 */
export function initContainContextMenuCallback(palette, containment, selector) {
	
	if (selector == undefined) {
		selector = defaultContainSelector;
	}
	
	/**
	 * Provides a link option for the context menu
	 */
	return function(event, contextMenu) {
		
		const selectedElements = hasLastSelected(selector());
		
		if (selectedElements.length > 0) {
			const parentElements = getParentElements(selectedElements);
			
			if (containment.canInsert(parentElements, selectedElements)) {
				contextMenu.addControl(event, "/github/kite9-org/kite9/client/behaviours/containers/contain/contain.svg", "Contain", 
					function(e2) {
						contextMenu.destroy();
						palette.open(
							event, 
							(e) => containment.canSurroundAll([e], parentElements, selectedElements), 		
						"contain");
					});
			}
		}
	}
}



