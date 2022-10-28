import { hasLastSelected, getContainedChildren, getNextSiblingId, getParentElement, getDependentElements } from "/public/bundles/api.js";
import { getMainSvg } from '../../../bundles/screen.js';


export function initDeleteContextMenuCallback(command, selector, cascade, orphan) {
	
	if (selector == undefined) {
		selector = function() {
			return getMainSvg().querySelectorAll("[id][k9-ui~='delete'].selected")
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
	function createDeleteStep(e, steps, cascade, allRemoved) {
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
			
			var removedChildren = getContainedChildren(e);
			removedChildren.forEach(r => {
				if ((allRemoved.indexOf(r) == -1) && (keptChildren.indexOf(r) == -1)) {
					allRemoved.push(r);
				}
			})
			allRemoved.push(id);
		} else if (id == undefined){
	       alert("Can't delete, element has no id");
	    } else if (parentElementId == undefined) {
	       alert("Can't delete, container has no id");
	    }
	}
	
	/**
	 * For any elements which connect to links, remove the links as they
	 * will have nothing to connect to.
	 */
	function removeDependentElements(allRemoved) {
		const newSteps = [];
		
		const implicated = getDependentElements(allRemoved);
		implicated.forEach(e => {
			var id = e.getAttribute("id");
			if (allRemoved.indexOf(id) == -1) {
				var parentElementId = getParentElement(e).getAttribute("id");
			
				newSteps.push({
					fragmentId: parentElementId,
					type: 'Delete',
					base64Element: command.getAdl(id),
					containedIds: [],
					beforeId: getNextSiblingId(e) 
				})
			}
		});
	
		return newSteps;
	}

	function performDelete(cm) {
		const steps = [];
		const allRemoved = [];
		selector().forEach(e => createDeleteStep(e, steps, cascade(e), allRemoved));
		
		if (steps.length > 0) {
			cm.destroy();
			const dependentSteps = removeDependentElements(allRemoved);
			const allSteps = dependentSteps.concat(steps);
			command.pushAllAndPerform(allSteps);
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


