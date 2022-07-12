import { isLink, isConnected, isTerminator, getKite9Target, parseInfo, createUniqueId, onlyUnique, getContainingDiagram } from '/github/kite9-org/kite9/client/bundles/api.js?v=v0.9'


/**
 * Allows you to drop a single element onto a link
 */
export function initLinkDropLocator() {
	
	return function(dragTargets, target) {
		if (dragTargets.length != 1) {
			return [];
		}
		
		if (!isConnected(dragTargets[0])) {
			return [];
		}
		
		var dropTarget = getKite9Target(target);
		
		if (isLink(dropTarget)) {
			var out = dropTarget.getAttribute("k9-ui");
			if (!out.includes("drop")) {
				return [];
			} else if (!isTerminator(dragTargets[0])) {
				return [ dropTarget ];
			}
		}

		return [ ];
	}	
}

/**
 * Handles dropping an element onto a link
 */
export function initLinkDropCallback(command) {
	
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
			"type": 'InsertUrlLink',
			"uriStr": "#"+dropId,
			"beforeId" : dropId,
			"newId": newId1,
			"fromId": ends[0],
			"toId": dragId,
			"fragmentId": diagramId
		})
		
		command.push({
			"type": 'InsertUrlLink',
			"uriStr": "#"+dropId,
			"beforeId" : dropId,
			"newId": newId2,
			"fromId": dragId,
			"toId": ends[1],
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