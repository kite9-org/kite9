import { hasLastSelected, getContainerChildren, getNextSiblingId, getParentElements, parseInfo } from '/public/bundles/api.js'
import { getMainSvg } from '/public/bundles/screen.js'
import { getElementUri } from '/public/classes/palette/palette.js';

/**
 * k9-palette attribute says which type of element this is.  The element can be replaced with another element of the same type.
 */
function initDefaultReplaceSelector() {
	return function() {
		return getMainSvg().querySelectorAll("[id].selected");
	}
}

function initDefaultReplaceChoiceSelector() {
	return function(palettePanel) {
		return palettePanel.querySelectorAll("[id][k9-palette]"); //  ~="+type+"]");	
	}
}


export function initReplaceContextMenuCallback(palette, command, rules, containment, replaceChoiceSelector, replaceSelector, createReplaceStep) {
	
	if (replaceChoiceSelector == undefined) {
		replaceChoiceSelector = initDefaultReplaceChoiceSelector();
	}
	
	if (replaceSelector == undefined) {
		replaceSelector = initDefaultReplaceSelector();
	}
	
	if (createReplaceStep == undefined) {
		createReplaceStep = function(command, e, drop, palettePanel) {			
			const uri = getElementUri(drop, palettePanel);	
			const eId = e.getAttribute('id');
			const info = parseInfo(e);
			
			if (!info.temporary) {
			
				command.push({
					"type": 'ReplaceTagUrl',
					"fragmentId": eId,
					"to": uri,
					"from": command.getAdl(eId),
					...rules
				});
				
				// delete any incompatible contents
				getContainerChildren(e)
					.filter(c => !containment.canContain(c, e))
					.forEach(c => {
						const deleteId = c.getAttribute("id");
						command.push({
							"type": "Delete",
							"fragmentId": eId,
							"beforeId": getNextSiblingId(c),
							"base64Element": command.getAdl(deleteId)
						})
					});
				
				return true;
			} else {
				return false;
			}
		}
	}
	
	/**
	 * Provides a link option for the context menu
	 */
	return function(event, contextMenu) {
		
		// this is the elements we are going to replace
		const selectedElements = hasLastSelected(replaceSelector());
		
		// this is the palette element we are going to replace it with
		const droppingElement = palette.get().querySelector("[id].mouseover");
		const palettePanel = palette.getOpenPanel();

		if (selectedElements.length > 0) {
			const parents = getParentElements(selectedElements);
			
			if (containment.canContainAll([droppingElement], parents)) {
				contextMenu.addControl(event, "/public/behaviours/selectable/replace/replace.svg",
					"Replace", 
					function(e2, selector) {
						contextMenu.destroy();
						 
						const result = Array.from(selectedElements)
							.map(e => createReplaceStep(command, e, droppingElement, palettePanel))
							.reduce((a, b) => a || b, false);
				
						if (result){
							palette.destroy();		
							command.perform();
							event.stopPropagation();
						}			
					
				});
			}
		}
	}
	
	
}




