import { hasLastSelected, createUniqueId, getParentElement, getNextSiblingId } from '/public/bundles/api.js'
import { getMainSvg } from '/public/bundles/screen.js'
import { getElementUri} from '/public/classes/palette/palette.js';


function defaultContainSelector() {
	return getMainSvg().querySelectorAll("[k9-palette].selected");
}

/**
 * Provides functionality so that when the user clicks on a 
 * palette element it is inserted into the document.
 */
export function initContainContextMenuCallback(palette, command, containment, containSelector) {
	
	if (containSelector == undefined) {
		containSelector = defaultContainSelector;
	}
	
	/**
	 * Creates the palette-element inside the container of the lastSelected element.
	 * Moves all the on-diagram selected elements inside it  
	 */
	return function(event, contextMenu) {
	
		const selectedElements = hasLastSelected(containSelector());
	    const lastSelectedElement = hasLastSelected(containSelector(), true);
		
		// this is the palette element we are going to contain them with
		const droppingElement = palette.get().querySelector("[id].lastSelected");
		const palettePanel = palette.getOpenPanel();		
		
		function createInsertStep(e, drop, newId) {
			return {
				"type": 'InsertUrl',
				"uriStr": getElementUri(drop, palettePanel),
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
	
		if (lastSelectedElement) {
			const parentElement = getParentElement(lastSelectedElement);
			const allowed = containment.canContain(droppingElement, parentElement);
			const newId = createUniqueId();
			
			if (allowed) {
				contextMenu.addControl(event, "/public/behaviours/containers/contain/contain.svg", "Contain", 
					function(e2) {
						contextMenu.destroy();
						// create the container element
						command.push(createInsertStep(lastSelectedElement, droppingElement, newId));
				
						// now move everything else into it
						Array.from(selectedElements)
							.filter(e => containment.canContain(e, droppingElement))
							.forEach(e => command.push(createContainStep(e, newId)));
							
						palette.destroy();		
						command.perform();
						event.stopPropagation();
					});
			}
		}
	
	}
}



