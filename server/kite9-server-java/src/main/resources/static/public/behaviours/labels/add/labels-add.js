import { getMainSvg } from '/public/bundles/screen.js'
import { hasLastSelected, parseInfo, getContainingDiagram, reverseDirection, createUniqueId } from '/public/bundles/api.js'


export function labelableSelector() {
	const labelables = Array.from(getMainSvg().querySelectorAll("[k9-ui~=label].selected"));
	return labelables;
}

export function createInsertLabelStep(e, templateUri, command) {
	const info = parseInfo(e);
	const end = info.end;
	const linkId = info.terminates ? info.terminates : e.getAttribute("id");
	const newId = createUniqueId();
	
	command.push({
		"type": 'InsertUrl',
		"fragmentId": linkId,
		"uriStr": templateUri,
		"newId" : newId,
	});
	
	if (end) {
		command.push({
			"type": "ReplaceAttr",
			"fragmentId": newId,
			"name": "end",
			"to" : end,
			"from" : e.getAttribute("end")
		});
	}
}


export function initAddLabelContextMenuCallback(command, templateUri, selector, action) {
	
	if (selector == undefined) {
		selector = labelableSelector;
	}
	
	if (action == undefined) {
		action = createInsertLabelStep;
	}
	
	/**
	 * Provides a label option for the context menu
	 */
	return function(event, contextMenu) {
		
		const selectedElements = hasLastSelected(selector());
		
		if (selectedElements.length > 0) {
			contextMenu.addControl(event, "/public/behaviours/labels/add/add.svg", "Add Label", 
				function(e2, selector) {
					contextMenu.destroy();
					selectedElements.forEach(e => action(e, templateUri, command));
					command.perform();
				}		
			)
		}
	}
}