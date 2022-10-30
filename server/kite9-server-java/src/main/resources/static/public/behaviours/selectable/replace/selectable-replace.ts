import { hasLastSelected, getContainerChildren, getNextSiblingId, getParentElement, parseInfo, isLink, isTerminator, isLabel, isConnected, onlyLastSelected } from '../../../bundles/api.js'
import { getMainSvg } from '../../../bundles/screen.js'
import { PaletteSelector, Selector } from '../../../bundles/types.js';
import { Command, SingleCommand } from '../../../classes/command/command.js';
import { Containment } from '../../../classes/containment/containment.js';
import { ContextMenuCallback } from '../../../classes/context-menu/context-menu.js';
import { getElementUri, Palette } from '../../../classes/palette/palette.js';

/**
 * k9-palette attribute says which type of element this is.  The element can be replaced with another element of the same type.
 */
function initDefaultReplaceSelector() {
	return function() {
		return Array.from(getMainSvg().querySelectorAll("[id].selected"));
	}
}

function initDefaultReplaceChoiceSelector() {
	return function(palettePanel: Element) {
		return Array.from(palettePanel.querySelectorAll("[id][k9-palette]")); //  ~="+type+"]");	
	}
}

export type CreateReplaceStep = (command: Command, e: Element, drop: Element, palettePanel: HTMLDivElement) => boolean;

export function initReplaceContextMenuCallback(
	palette: Palette, 
	command: Command, 
	rules: SingleCommand, 
	containment: Containment, 
	replaceChoiceSelector : PaletteSelector = undefined, 
	replaceSelector: Selector = undefined, 
	createReplaceStep: CreateReplaceStep = undefined , 
	replaceChecker : (e1: Element, e2: Element) => boolean = undefined)
		: ContextMenuCallback {
	
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
				return containment.allowed([newElement], [oldParent], children).length > 0;
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
					function() {
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




