import { getMainSvg } from '../../../bundles/screen.js'
import { hasLastSelected, parseInfo,  createUniqueId } from '../../../bundles/api.js'
import { ContextMenu, ContextMenuCallback } from '../../../classes/context-menu/context-menu.js';
import { Command } from '../../../classes/command/command.js';
import { Selector } from '../../../bundles/types.js';

export function labelableSelector() : SVGGraphicsElement[] {
	const labelables = Array.from(getMainSvg().querySelectorAll("[k9-ui~=label].selected")) as SVGGraphicsElement[];
	return labelables;
}

export function createInsertLabelStep(e: Element, templateUri: string, command: Command) {
	const info = parseInfo(e);
	const end = info.end;
	const linkId = info.terminates ? info.terminates : e.getAttribute("id");
	const customLabel = e.getAttribute("k9-label");
	const newId = createUniqueId();
	const labelUri = customLabel ? customLabel : templateUri;
	
	command.push({
		"type": 'InsertUrl',
		"fragmentId": linkId,
		"uriStr": labelUri,
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


export function initAddLabelContextMenuCallback(
	command: Command, 
	templateUri: string, 
	selector : Selector = labelableSelector, 
	action = createInsertLabelStep) : ContextMenuCallback {
	
	if (selector == undefined) {
		selector = labelableSelector;
	}
	
	if (action == undefined) {
		action = createInsertLabelStep;
	}
	
	/**
	 * Provides a label option for the context menu
	 */
	return function(event: Event, contextMenu: ContextMenu) {
		
		const selectedElements = hasLastSelected(selector());
		
		if (selectedElements.length > 0) {
			contextMenu.addControl(event, "/public/behaviours/labels/add/add.svg", "Add Label", 
				function() {
					contextMenu.destroy();
					selectedElements.forEach(e => action(e, templateUri, command));
					command.perform();
				}		
			)
		}
	}
}