import { getSVGCoords, getMainSvg } from '/github/kite9-org/kite9/client/bundles/screen.js?v=v0.5'
import { handleTransformAsStyle, getKite9Target, getParentElement, getNextSiblingId, onlyUnique, isLink, isConnected } from '/github/kite9-org/kite9/client/bundles/api.js?v=v0.5'
import { getBeforeId } from '/github/kite9-org/kite9/client/bundles/ordering.js?v=v0.5'

export function initContainerDropLocatorFunction(containment) {

	return function (dragTarget, dropTarget) {
		if (dropTarget == null) {
			return false;
		}
		
		if (dragTarget==dropTarget) {
			return false;
		}
		
		if (!containment.canContain(dragTarget, dropTarget)) {
			return false;
		}

		return true;
	}
}

export function initContainerDropCallback(command, containment) {
	
	return function(dragState, evt, dropTargets) {
		const dragTargets = dragState.map(s => s.dragTarget);
		const connectedDropTargets = dropTargets.filter(t => {
			return containment.canContainAll(dragTargets, t);
		}).filter(onlyUnique);
		
		if (connectedDropTargets.length > 0) {
			const dropTarget = connectedDropTargets[0];
			var beforeId = getBeforeId(dropTarget, evt, dragTargets);
			Array.from(dragState).forEach(s => {
				if (s.dragParentId) {
					// we are moving this from somewhere else in the diagram
					command.push( {
						type: 'Move',
						to: dropTarget.getAttribute('id'),
						moveId: s.dragTarget.getAttribute('id'),
						toBefore: beforeId,
						from: s.dragParentId,
						fromBefore: s.dragBeforeId
					});
				} else if (s.url){
					// we are inserting this into the diagram
					command.push({
						type: 'InsertUrl',
						beforeId: beforeId,
						fragmentId: dropTarget.getAttribute('id'),
						uriStr: s.url,
						newId: s.dragTarget.getAttribute('id')
					});
				}
			});
			return true;
		} else {
			return false;
		}
	}
}

