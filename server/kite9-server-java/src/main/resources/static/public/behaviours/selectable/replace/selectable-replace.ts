import { hasLastSelected, getContainerChildren, getNextSiblingId, getParentElement, parseInfo, isLink, isTerminator, isLabel, isConnected } from '../../../bundles/api.js'
import { getMainSvg } from '../../../bundles/screen.js'
import { getElementUri } from '../../../classes/palette/palette.js';

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

export function initReplaceContextMenuCallback(palette, command, rules, containment, replaceChoiceSelector, replaceSelector, createReplaceStep, replaceChecker) {
	
	if (replaceChoiceSelector == undefined) {
		replaceChoiceSelector = initDefaultReplaceChoiceSelector();
	}
	
	if (replaceSelector == undefined) {
		replaceSelector = initDefaultReplaceSelector();
	}
	
	if (replaceChecker == undefined) {
		replaceChecker = function(oldElement, newElement) {
			if (isLink(oldElement) && isLink(newElement)) {
				return true;
			}
			
			if (isTerminator(oldElement) && isTerminator(newElement)) {
				return true;
			}
			
			if ((isLabel(oldElement) && isLabel(newElement)) ||
				(isConnected(oldElement) && isConnected(newElement))) {
				const oldParent = getParentElement(oldElement);
				const children = getContainerChildren(oldElement);
				return containment.allowed([newElement], [oldParent], children);
			}
		}
		
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
		const lastSelectedElement = onlyLastSelected(replaceSelector());
		
		// this is the palette element we are going to replace it with
		const droppingElement = palette.get().querySelector("[id].lastSelected");
		const palettePanel = palette.getOpenPanel();

		if (lastSelectedElement) {			
			if (replaceChecker(lastSelectedElement, droppingElement)) {
				contextMenu.addControl(event, "/public/behaviours/selectable/replace/replace.svg",
					"Replace", 
					function(e2, selector) {
						contextMenu.destroy();
						 
						const result = Array.from(selectedElements)
							.filter(e => replaceChecker(e, droppingElement))
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




