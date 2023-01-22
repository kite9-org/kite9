import { isLink, isConnected, getKite9Target, parseInfo, createUniqueId, onlyUnique, getContainingDiagram, getAffordances } from '../../../bundles/api.js'
import { currentTargets } from '../../../bundles/screen.js';
import { Command } from '../../../classes/command/command.js';
import { DropCallback, DropLocatorCallback } from '../../../classes/dragger/dragger.js';


/**
 * Allows you to drop a single element onto a link
 */
export function initLinkDropLocator() : DropLocatorCallback {
	
	return function(dragTargets, event) {
		if (dragTargets.length != 1) {
			return null;
		}
		
		if (!isConnected(dragTargets[0])) {
			return null;
		}
		
		const dropLinkTargets = currentTargets(event)
			.map(t => getKite9Target(t))
			.filter(t => isLink(t));
		
		if (dropLinkTargets.length > 0) {
			const out = getAffordances(dropLinkTargets[0]);
			if (!out.includes("drop")) {
				return null;
			} else {
				return dropLinkTargets[0];
			}
		}

		return null;
	}	
}

/**
 * Handles dropping an element onto a link
 */
export function initLinkDropCallback(command: Command) : DropCallback {
	
	return function(dragState, evt, dropTargets) {
		const connectionDropTargets = dropTargets
			.filter(t => isLink(t))
			.filter(onlyUnique);

		
		if (connectionDropTargets.length != 1) {
			return;
		}
		
		if (dragState.length != 1) {
			return;
		}
		
		const dragTargets = dragState.map(s => s.dragTarget);
		const drop = connectionDropTargets[0];
		const drag = dragTargets[0];
		const info = parseInfo(drop);
		const ends = info['link'];
		
		const dragId = drag.getAttribute("id");
		const dropId = drop.getAttribute("id");
		const diagramId = getContainingDiagram(connectionDropTargets[0]).getAttribute("id");
		
		if (ends.indexOf(dragId) > -1) {
			// already connected
			return;
		}
		
		// first, duplicate the link twice
		const newId1 = createUniqueId();
		const newId2 = createUniqueId();
		
		command.push({
			"type": 'InsertUrlWithChanges',
			"uriStr": "#"+dropId,
			"beforeId" : dropId,
			"newId": newId1,
			"xpathToValue": {
					"*[local-name()='from']/@reference": ends[0],
					"*[local-name()='to']/@reference": dragId
			},
			"fragmentId": diagramId
		})
		
		command.push({
			"type": 'InsertUrlWithChanges',
			"uriStr": "#"+dropId,
			"beforeId" : dropId,
			"xpathToValue": {
					"*[local-name()='from']/@reference": dragId,
					"*[local-name()='to']/@reference": ends[1]
			},
			"newId": newId2,
			"fragmentId": diagramId
		})
		
		// delete old link
		command.push({
			"type": "Delete",
			"fragmentId": diagramId,
			"base64Element": command.getAdl(dropId)
		})
	}
	
}