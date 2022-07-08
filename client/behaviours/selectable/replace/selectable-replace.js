import { hasLastSelected, getContainerChildren, getNextSiblingId, getParentElements, parseInfo } from '/github/kite9-org/kite9/bundles/api.js?v=v0.2'
import { getMainSvg } from '/github/kite9-org/kite9/bundles/screen.js?v=v0.2'

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


export function initReplacePaletteCallback(command, rules, containment, replaceChoiceSelector, replaceSelector, createReplaceStep) {
	
	if (replaceChoiceSelector == undefined) {
		replaceChoiceSelector = initDefaultReplaceChoiceSelector();
	}
	
	if (replaceSelector == undefined) {
		replaceSelector = initDefaultReplaceSelector();
	}
	
	if (createReplaceStep == undefined) {
		createReplaceStep = function(command, e, drop, palettePanel) {			
			const paletteId = palettePanel.getAttribute("id");
			const id = drop.getAttribute("id");
			const uri = palettePanel.getAttribute("k9-palette-uri")+"?format=adl#"+id.substring(0, id.length - paletteId.length);	
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
	
	return function(palette, palettePanel, type) {
		function click(event) {
			if (palette.getCurrentAction() == "replace") {
				const selectedElements = replaceSelector();
				const droppingElement = palette.get().querySelector("[id].mouseover");
				const result = Array.from(selectedElements)
					.map(e => createReplaceStep(command, e, droppingElement, palettePanel))
					.reduce((a, b) => a || b, false);
				
				if (result){
					palette.destroy();		
					command.perform();
					event.stopPropagation();
				}
			}
		}
	
		replaceChoiceSelector(palettePanel).forEach(function(v) {
	    	v.removeEventListener("click", click);
	    	v.addEventListener("click", click);
		})
	}
	
	
	
}

/**
 * Adds replace option into context menu
 */
export function initReplaceContextMenuCallback(palette, containment, selector) {
	
	if (selector == undefined) {
		selector = initDefaultReplaceSelector();
	}
	
	/**
	 * Provides a link option for the context menu
	 */
	return function(event, contextMenu) {
		
		const selectedElements = hasLastSelected(selector());

		if (selectedElements.length > 0) {
			const parents = getParentElements(selectedElements);
			
			if (containment.canContainAll(selectedElements, parents)) {
				// console.log("Allowing replace with types: "+allowedTypes);
				contextMenu.addControl(event, "/public/behaviours/selectable/replace/replace.svg",
						"Replace", 
						function(e2, selector) {
					contextMenu.destroy();
					
					palette.open(
						event, 
						(e) => containment.canContain(e, parents),
						"replace");
				});
			}
		}
	}
}



