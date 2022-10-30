import { hasLastSelected, createUniqueId } from '../../../bundles/api.js'
import { getMainSvg } from '../../../bundles/screen.js'
import { Selector } from '../../../bundles/types.js'
import { Command } from '../../../classes/command/command.js';
import { ContextMenu, ContextMenuCallback } from '../../../classes/context-menu/context-menu.js';


const defaultChildSelector : Selector = function() {
	return Array.from(getMainSvg().querySelectorAll("[k9-child].selected"));
}

/**
 * Adds child option into context menu
 */
export function initChildContextMenuCallback(
	command: Command, 
	selector: Selector = defaultChildSelector) : ContextMenuCallback {

	function getElementUri(e: Element) {
		return e.getAttribute("k9-child");
	}

	function createInsertStep(e: Element, uri: string) {
		return {
			"type": 'InsertUrl',
			"fragmentId": e.getAttribute('id'),
			"uriStr": uri,
			"deep": true,
			"newId": createUniqueId()
		}
	}

	/**
	 * Provides a link option for the context menu
	 */
	return function(event: Event, contextMenu: ContextMenu) {

		const selectedElements = hasLastSelected(selector());

		if (selectedElements.length > 0) {
			contextMenu.addControl(event, "/public/behaviours/containers/child/add.svg", "Add Child",
				function() {
					selectedElements.forEach(e => {
						const uri = getElementUri(e);
						if (uri != undefined) {
							command.push(createInsertStep(e, uri));
						}
					});
					command.perform();
					contextMenu.destroy();
				});
		}
	}
}



