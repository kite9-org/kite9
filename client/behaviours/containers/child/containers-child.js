import { hasLastSelected, createUniqueId } from '/github/kite9-org/kite9/client/bundles/api.js?v=v0.3'
import { getMainSvg } from '/github/kite9-org/kite9/client/bundles/screen.js?v=v0.3'


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
			contextMenu.addControl(event, "/public/client/behaviours/containers/child/add.svg", "Add Child", 
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



