import { hasLastSelected, getContainedChildIds, getNextSiblingId, getParentElement, getDependentElements } from "../../../bundles/api.js";
import { getMainSvg } from '../../../bundles/screen.js';
import { Command, SingleCommand } from "../../../classes/command/command.js";
import { ElementFilter, Selector } from "../../../bundles/types.js";
import { getAffordances } from "../../../bundles/api.js";
import { ContextMenu, ContextMenuCallback } from "../../../classes/context-menu/context-menu.js";


export function initDeleteContextMenuCallback(
	command: Command,
	selector: Selector = undefined,
	cascade: ElementFilter = undefined,
	orphan: ElementFilter = undefined) : ContextMenuCallback {

	if (selector == undefined) {
		selector = function() {
			return Array.from(getMainSvg().querySelectorAll("[id][k9-ui~='delete'].selected"))
		}
	}

	if (cascade == undefined) {
		cascade = function(e) {
			const ui = getAffordances(e);
			return ui.includes('cascade')
		}
	}

	if (orphan == undefined) {
		orphan = function(e) {
			const ui = getAffordances(e);
			return ui.includes('orphan')
		}
	}

	/**
	 * Takes a node and creates a delete command.
	 */
	function createDeleteStep(
		e: Element,
		steps: SingleCommand[],
		cascade: boolean,
		allRemoved: string[]) {

		const id = e.getAttribute("id");
		const keptChildren = cascade ? [] : getContainedChildIds(e, e => !orphan(e));
		const parentElementId = getParentElement(e).getAttribute("id");

		if ((id != undefined) && (parentElementId != undefined)) {
			steps.push({
				fragmentId: parentElementId,
				type: 'Delete',
				base64Element: command.getAdl(id),
				containedIds: keptChildren,
				beforeId: getNextSiblingId(e)
			});

			const removedChildren = getContainedChildIds(e);
			removedChildren.forEach(r => {
				if ((allRemoved.indexOf(r) == -1) && (keptChildren.indexOf(r) == -1)) {
					allRemoved.push(r);
				}
			})
			allRemoved.push(id);
		} else if (id == undefined) {
			alert("Can't delete, element has no id");
		} else if (parentElementId == undefined) {
			alert("Can't delete, container has no id");
		}
	}

	/**
	 * For any elements which connect to links, remove the links as they
	 * will have nothing to connect to.
	 */
	function removeDependentElements(allRemoved: string[]) : SingleCommand[] {
		const newSteps = [];

		const implicated = getDependentElements(allRemoved);
		implicated.forEach(e => {
			const id = e.getAttribute("id");
			if (allRemoved.indexOf(id) == -1) {
				const parentElementId = getParentElement(e).getAttribute("id");

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

	function performDelete(cm: ContextMenu) {
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

		if (e.length > 0) {
			cm.addControl(event, "/public/behaviours/selectable/delete/delete.svg", 'Delete', () => performDelete(cm));
		}
	}
}


