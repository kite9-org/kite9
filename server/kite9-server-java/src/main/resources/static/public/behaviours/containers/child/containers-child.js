import { hasLastSelected, createUniqueId } from '/public/bundles/api.js'
import { getMainSvg } from '/public/bundles/screen.js'


function defaultChildSelector() {
	return getMainSvg().querySelectorAll("[k9-child].selected");
}

/**
 * Adds child option into context menu
 */
export function initChildContextMenuCallback(command, selector) {
	
	if (selector == undefined) {
		selector = defaultChildSelector;
	}
	
	function getElementUri(e) {
		return e.getAttribute("k9-child");
	}
	
	function createInsertStep(e, uri) {
		return {
			"type": 'InsertUrl',
			"fragmentId": e.getAttribute('id'),
			"uriStr": uri,
			"deep" : true,
			"newId": createUniqueId() 
		}
	}
	
	/**
	 * Provides a link option for the context menu
	 */
	return function(event, contextMenu) {
		
		const selectedElements = hasLastSelected(selector());
		
		if (selectedElements.length > 0) {
			contextMenu.addControl(event, "/public/behaviours/containers/child/add.svg", "Add Child", 
				function(e2) {
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



