import { getMainSvg } from '/github/kite9-org/kite9/client/bundles/screen.js?v=v0.4'
import { hasLastSelected, parseInfo, getContainingDiagram, reverseDirection, createUniqueId } from '/github/kite9-org/kite9/client/bundles/api.js?v=v0.4'


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


export function initLabelContextMenuCallback(command, templateUri, selector, action) {
	
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
			contextMenu.addControl(event, "/public/behaviours/labels/label.svg", "Add Label", 
				function(e2, selector) {
					contextMenu.destroy();
					selectedElements.forEach(e => action(e, templateUri, command));
					command.perform();
				}		
			)
		}
	}
}