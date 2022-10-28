import { getSVGCoords, getElementPageBBox, getMainSvg, closestSide } from '../../../bundles/screen.js'
import { handleTransformAsStyle, getKite9Target, getParentElement, getNextSiblingId, onlyUnique, isLink, isConnected, isPort, getAffordances, parseInfo } from '../../../bundles/api.js'
import { parseStyle } from '../../../bundles/css.js'
import { drawBar, clearBar } from  '../../../bundles/ordering.js'


/**
 * Essentially the same as initContainerDropCallback, except that
 * you can also position the port at the same time.
 */
export function initPortDropCallback(command, containment) {
	
	return function(dragState, evt, dropTargets) {
		const dragTargets = dragState.map(s => s.dragTarget);
		const connectedDropTargets = dropTargets.filter(t => {
			return containment.canContainAll(dragTargets, t);
		}).filter(onlyUnique);
		
		if (connectedDropTargets.length > 0) {
			const dropTarget = connectedDropTargets[0];
			const side = closestSide(dropTarget, getSVGCoords(evt));
			Array.from(dragState).forEach(s => {
				if (s.dragParentId) {
					// we are moving this from somewhere else in the diagram
					command.push( {
						type: 'Move',
						to: dropTarget.getAttribute('id'),
						moveId: s.dragTarget.getAttribute('id'),
						from: s.dragParentId,
						fromBefore: s.dragBeforeId
					});
					
					// new bit for ports
					const adlElement = command.getADLDom(s.dragTarget.getAttribute("id"))
					const canReposition = getAffordances(s.dragTarget).includes("port");
					const style = parseStyle(adlElement.getAttribute("style"));
	
					if (canReposition) {
						command.push({
							fragmentId: s.dragTarget.getAttribute("id"),
							type: 'ReplaceStyle',
							name: '--kite9-direction',
							to: side,
							from: style['--kite9-direction']
						});
					}
					
				} else if (s.url){
					// we are inserting this into the diagram
					command.push({
						type: 'InsertUrl',
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


export function initPortMoveCallback(containment) {

	function updateBar(inside, side) {
		const shape = inside.querySelector('.k9-shape');
		const { x, y, width, height } = getElementPageBBox(shape); 
	
		switch(side) {
			case 'top':
				drawBar(0, 0, width, 0, inside);
				return;
			case 'right':
				drawBar(width, 0, width, height, inside);			
				return;
			case 'bottom':
				drawBar(0, height, width, height, inside);			
				return;
			case 'left':
				drawBar(0, 0, 0, height, inside);			
				return;
		}
	}
	
	return function (dragTargets, event, dropTargets, barDirectionOverrideHoriz) {
		if (dragTargets.filter(dt => isPort(dt)).length == 0) {
			// not dragging a port
			return;
		}
		
		if (dropTargets) {
			const connectedDropTargets = dropTargets.filter(t => {
					return containment.canContainAll(dragTargets, t);
				}).filter(onlyUnique);
			
			if ((connectedDropTargets.length > 0)) {
				const dropInto = connectedDropTargets[0];
				const side = closestSide(dropInto, getSVGCoords(event));
				updateBar(dropInto, side);
				return
			}
		}
		
		clearBar();

	}

}

