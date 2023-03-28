import { getKite9Target } from '../../../bundles/api.js'
import { getBeforeId } from '../../../bundles/ordering.js'
import { DropCallback, DropLocatorCallback } from '../../../classes/dragger/dragger.js';
import { Command } from '../../../classes/command/command.js';
import { Containment } from '../../../classes/containment/containment.js';
import { ElementBiFilter } from '../../../bundles/types.js';
import { currentTargets } from '../../../bundles/screen.js';

/**
 * When dragging a connected over some on-screen containers, this will
 * work out the top-most one that can contain the connected.
 */
export function initContainerDropLocatorCallback(containment: Containment): DropLocatorCallback {

	return function(dragTargets, event) {
		const containerDropTargets = currentTargets(event)
			.map(t => getKite9Target(t))
			.filter(t => t != undefined);
				
		for (let i = 0; i < containerDropTargets.length; i++) {
			const cdt = containerDropTargets[i];
			if (dragTargets.indexOf(cdt) == -1) {
				if (containment.canContainAll(dragTargets, cdt)) {
					return cdt;
				}
			}
		}

		return null;
	}
}

/**
 * Extensible function that supports filtering and adding an onwards-chain drop callback.
 */
export function initContainmentDropCallback(
	command: Command,
	filter: ElementBiFilter = () => true): DropCallback {

	return function(dragState, evt, dropTargets) {
		const relevantState = dragState
			.filter(si => filter(si.dragTarget));

		const dragTargets = relevantState
			.map(s => s.dragTarget)

		dropTargets.forEach(dropTarget => {
			const beforeId = getBeforeId(dropTarget, evt, dragTargets);
			dragState.forEach(s => {
				if (filter(s.dragTarget, dropTarget)) {
					console.log("Invoking containment drop callback")
					if (s.dragParentId) {
						// we are moving this from somewhere else in the diagram
						command.push({
							type: 'Move',
							to: dropTarget.getAttribute('id'),
							moveId: s.dragTarget.getAttribute('id'),
							toBefore: beforeId,
							from: s.dragParentId,
							fromBefore: s.dragBeforeId
						});
					} else if (s.url) {
						// we are inserting this into the diagram
						command.push({
							type: 'InsertUrl',
							beforeId: beforeId,
							fragmentId: dropTarget.getAttribute('id'),
							uriStr: s.url,
							newId: s.dragTarget.getAttribute('id')
						});
					}
				}
			})		
		})
	}
}

