import { hasLastSelected, getContainedChildren, getNextSiblingId, getParentElement } from "/public/client/bundles/api.js";


export function initDeleteContextMenuCallback(command, selector, cascade, orphan) {
	
	if (selector == undefined) {
		selector = function() {
			return document.querySelectorAll("[id][k9-ui~='delete'].selected")
		}
	}
	
	if (cascade == undefined) {
		cascade = function(e) {
			var ui = e.getAttribute("k9-ui");
			return (ui == undefined ? "" : ui).includes('cascade')
		}
	}
	
	if (orphan == undefined) {
		orphan = function(e) {
			var ui = e.getAttribute("k9-ui");
			return (ui == undefined ? "" : ui).includes('orphan')
		}
	}
	
	/**
	 * Takes a node and creates a delete command.
	 */
	function createDeleteStep(e, steps, cascade) {
		var id = e.getAttribute("id");
		var keptChildren = cascade ? [] : getContainedChildren(e, e=> !orphan(e));
		var parentElementId = getParentElement(e).getAttribute("id");
    
		if ((id != undefined) && (parentElementId != undefined)) {
			steps.push({
				fragmentId: parentElementId,
				type: 'Delete',
				base64Element: command.getAdl(id),
				containedIds: keptChildren,
				beforeId: getNextSiblingId(e) 
			});
		} else if (id == undefined){
       alert("Can't delete, element has no id");
    } else if (parentElementId == undefined) {
       alert("Can't delete, container has no id");
    }
	}

	function performDelete(cm) {
		var steps = [];
		selector().forEach(e => createDeleteStep(e, steps, cascade(e)));
		
		if (steps.length > 0) {
			cm.destroy();
			command.pushAllAndPerform(steps);
			// console.log("delete complete");
		}
	}
	
	/**
	 * Provides a delete option for the context menu
	 */
	return function(event, cm) {
		
		const e = hasLastSelected(selector());
		
		if (e.length > 0){
			cm.addControl(event, "/public/behaviours/selectable/delete/delete.svg", 'Delete', () => performDelete(cm));
		}
	}
}


