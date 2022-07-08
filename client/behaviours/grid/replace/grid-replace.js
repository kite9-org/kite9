import { getMainSvg } from '/github/kite9-org/kite9/client/bundles/screen.js?v=v0.2'
import { createUniqueId, parseInfo, getKite9Target, hasLastSelected } from '/github/kite9-org/kite9/client/bundles/api.js?v=v0.2'

/**
 * Allows you to select temporary grid elements.
 */
export function defaultReplaceCellSelector() {
	return getMainSvg().querySelectorAll("[k9-info*='layout: GRID'] > .grid-temporary.selected");
}

function initDefaultReplaceChoiceSelector() {
	return function(palettePanel) {
		return palettePanel.querySelectorAll("[id][k9-palette~=cell]");	
	}
}

export function initGridTemporaryReplacePaletteCallback(command, replaceChoiceSelector, replaceSelector) {
	
	if (replaceChoiceSelector == undefined) {
		replaceChoiceSelector = initDefaultReplaceChoiceSelector();
	}
	
	
	if (replaceSelector == undefined) {
		replaceSelector = defaultReplaceCellSelector;
	}
	
	function createReplaceStep (command, e, drop, palettePanel) {	
		const paletteId = palettePanel.getAttribute("id");
		const id = drop.getAttribute("id");
		const uri = palettePanel.getAttribute("k9-palette-uri")+"?format=adl#"+id.substring(0, id.length - paletteId.length);	

		if (e.classList.contains("grid-temporary")) {
			
			const newId = createUniqueId();
			const parent = getKite9Target(e.parentElement);
			const rectInfo = parseInfo(e);
			const position = rectInfo['position'];
			const newOccupies = position[0]+ " " + position[1] + " " +
				position[2]+ " " + position[3] + " ";
			
			command.push({
				"type": 'InsertUrl',
				"fragmentId": parent.getAttribute('id'),
				"uriStr": uri,
				"newId": newId,
			});
			
			command.push({
				"type" : "ReplaceStyle",
				"fragmentId" : newId,
				"name" : "kite9-occupies",
				"to" : newOccupies
			});
			
			return true;
		} else {
			return false;
		}
		
	}
	
	return function(palette, palettePanel) {
		function click(event) {
			if (palette.getCurrentAction() == 'replace') {
				const selectedElements = replaceSelector();
				const droppingElement = palette.get().querySelector("[id].mouseover")
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
	
