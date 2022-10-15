import { getSVGCoords, getElementPageBBox, getMainSvg } from '/public/bundles/screen.js'
import { handleTransformAsStyle, getKite9Target, getParentElement, getNextSiblingId, onlyUnique, isLink, isConnected, isPort, getAffordances, parseInfo } from '/public/bundles/api.js'
import { parseStyle } from '/public/bundles/css.js'
import { drawBar, clearBar } from  '/public/bundles/ordering.js'


function closestSide(dropTarget, event) {
	const OUT_OF_BOUNDS = 100000;
	const eventCoords = getSVGCoords(event);
	const boxCoords = getElementPageBBox(dropTarget);
	
	const topDist = eventCoords.y - boxCoords.y; 
	const leftDist = eventCoords.x - boxCoords.x; 
	const bottomDist = boxCoords.y +  boxCoords.height - eventCoords.y;
	const rightDist = boxCoords.x + boxCoords.width - eventCoords.x;
	
	const dists = {
		'top': topDist,
		'right': rightDist, 
		'bottom': bottomDist, 
		'left': leftDist 
	};
	
	const bestSide = ['top', 'right', 'bottom', 'left']
		.filter(o => dists[o] > 0)
		.reduce((a, b) => dists[a] < dists[b] ? a : b, 'top');
		
	return bestSide;
}

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
			const side = closestSide(dropTarget, evt);
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
							name: '--kite9-port-side',
							to: side,
							from: style['--kite9-port-side']
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
				const side = closestSide(dropInto, event);
				updateBar(dropInto, side);
				return
			}
		}
		
		clearBar();

	}

}

