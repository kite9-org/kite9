import { parseInfo, isCell, isGrid, getKite9Target, isConnected } from '../../../bundles/api.js'
import { drawBar, clearBar } from '../../../bundles/ordering.js'
import { getElementPageBBox, getSVGCoords, getMainSvg, currentTargets } from '../../../bundles/screen.js'
import { Direction, Selector, Point, Area } from '../../../bundles/types.js';
import { Command } from '../../../classes/command/command.js';
import { DragLocatorCallback, DropCallback, DropLocatorCallback, MoveCallback } from '../../../classes/dragger/dragger.js';
import { getOrdinal, getOrdinals, Ordinals  } from '../common-grid.js' 

function isEmptyGrid(e) {
	if (isGrid(e)) {
		return calculateOccupation(e).length==0;
	} 
	
	return false;
}

export function initCellDropLocatorCallback() : DropLocatorCallback {
	
	// we allow cells to be dropped onto other cells, as this instigates the 
	// re-arrage functionality
	
	return function(dragTargets, e) {
		const containerDropTargets = currentTargets(e)
				.map(t => getKite9Target(t))
				.filter(t => isConnected(t));
				
		if (isCell(dragTargets[0]) && isCell(containerDropTargets[0])) {
			return containerDropTargets[0];
		} else {
			return null;
		}
	}
	
}

type Occupation = {
	x: number[],
	y: number[]
}[]

type GridArea = {
	x: [number, number],
	y: [number, number],
	items: { [ index: string] : { dx: [number, number], dy: [number, number] }}
}

type MoveCache = {
	area?: GridArea,
	mover?: Element,
	container?: Element
	occupation?: Occupation,
	side?: Direction
}

let moveCache : MoveCache = {
	area: null,
	mover: null, 
	container: null,
	occupation: [],
	side: null
}

/**
 * This will only allow you to drag cells from the container which is the mover.
 */
export function initCellDragLocator(selector: Selector = undefined) : DragLocatorCallback {
	
	if (selector == undefined) {
		selector = function() {
			moveCache = {};
			const allCells = Array.from(getMainSvg().querySelectorAll("[id][k9-ui~='cell'].selected, [id][k9-ui~='cell'].mouseover"))
			if (allCells.length > 0) {
				const mover = allCells.filter(dt => dt.classList.contains("lastSelected"))[0];
				const container = mover.parentElement;
				const cellsInContainer = allCells.filter(c => c.parentElement == container);
				return cellsInContainer;
			} else {
				return [];
			}
		}
	}
	
	return function() {
		const out = selector();
		return out;
	}
}

function calculateOccupation(container: Element) : Occupation {
	
	if (moveCache.container == container) {
		return moveCache.occupation;
	}
	
	const occupation = [];
	
	Array.from(container.children).forEach(e => {
		const details = parseInfo(e);
		if ((details != null) && details['grid-x']) {
			const gridX = details['grid-x'];
			const gridY = details['grid-y'];
			
			if (!e.classList.contains('grid-temporary')) {
				occupation.push({x: gridX, y: gridY})
			}
		}
	});
	
	moveCache.container = container;
	moveCache.occupation = occupation;
	moveCache.side = undefined;
	
	return occupation;
}

export function initCellMoveCallback() : MoveCallback {
	
	function calculateArea(dragTargets: Element[]) {
		
		function up1(area: [number, number], change: [number, number]) {
			area[0] = Math.min(area[0], change[0]);
			area[1] = Math.max(area[1], change[1]);
		}
		
		const mover = dragTargets.filter(dt => dt.classList.contains("lastSelected"))[0];
		
		if (moveCache.mover == mover) {
			return moveCache.area;
		}
		
		const moverInfo = parseInfo(mover);
		const moverX = moverInfo ? moverInfo['grid-x'][0] : 0;
		const moverY = moverInfo ? moverInfo['grid-y'][0] : 0;
		
		const out: GridArea = {
			x: [10000, -10000],
			y: [10000, -10000],
			items: {}
		}
		
		dragTargets.forEach(dt => {
			const id = dt.getAttribute("id");
			if (dt['container'] == mover['container']) {
				const dragInfo = parseInfo(dt);
				const dragX = dragInfo['grid-x'];
				const dragY = dragInfo['grid-y'];
				
				up1(out.x, [dragX[0]-moverX, dragX[1]-moverX]);
				up1(out.y, [dragY[0]-moverY, dragY[1]-moverY]);
				
				out.items[id] = {
					dx: [ dragX[0] - moverX, dragX[1] - moverX ],
					dy: [ dragY[0] - moverY, dragY[1] - moverY ] 
				}
			} 
		});
		
		moveCache.mover = mover;
		moveCache.area = out;
		
		return out;
		
	}

	function overlaps(dragTargets: Element[], dropTargets: Element[]) : boolean {
		
		function intersects(r1, r2) {
			const startIn = (r1[0] >= r2[0]) && (r1[0] < r2[1]);
			const endIn = (r1[1] > r2[0]) && (r1[1] <= r2[1]);
			return startIn || endIn;
		}
		
		const container = dropTargets[0].parentElement;
		const area = calculateArea(dragTargets);
		const occupation = calculateOccupation(container);
		
		const dropInfo = parseInfo(dropTargets[0]);
		const dropX = dropInfo['grid-x'];
		const dropY = dropInfo['grid-y'];
		
		for(let i = 0; i < dragTargets.length; i++) {
			const dt = dragTargets[i];
			const id = dt.getAttribute("id");
			const item = area.items[id];
			const movedItem = { 
					dx: [item.dx[0] + dropX[0], item.dx[1] + dropX[0]],
					dy: [item.dy[0] + dropY[0], item.dy[1] + dropY[0]]
			}
			if (item != undefined) {
				for(let j = 0; j < occupation.length; j++) {
					const occ = occupation[j];
					const out = intersects(movedItem.dx, occ.x) && 
						intersects(movedItem.dy, occ.y);
					if (out) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	function closestSide(pos: Point, box: Area) : Direction {
		const ld = pos.x - box.x;
		const rd = box.x + box.width - pos.x;
		const ud = pos.y - box.y;
		const dd = box.y + box.height - pos.y;
		
		if ((ld <= rd) && (ld <= ud) && (ld <= dd)) {
			return 'left';
		} else if ((rd <= ud) && (rd <= dd)) {
			return 'right';
		} else if ((ud <= dd)) {
			return 'up';
		} else {
			return 'down';
		}		
	}
	
	return function (dragTargets, event, dropTargets) {
		if (event == undefined) {
			clearBar();
			return;
		}
		
		const cellDropTargets = dropTargets.filter(dt => isCell(dt));
		const cellDragTargets = dragTargets.filter(dt => isCell(dt));
		const gridDropTargets = dropTargets.filter(dt => isEmptyGrid(dt));
		const pos = getSVGCoords(event);
		if (cellDragTargets.length == dragTargets.length) {
			if (cellDropTargets.length == 1) {
				if (overlaps(cellDragTargets, cellDropTargets)) {
					// draw a bar on the closest side 
					const box = getElementPageBBox(cellDropTargets[0]);
					moveCache.side = closestSide(pos, box);
					switch (moveCache.side) {
					case 'up':
						return drawBar(box.x, box.y, box.x + box.width, box.y);
					case 'down':
						return drawBar(box.x, box.y+box.height, box.x + box.width, box.y+box.height);
					case 'left':
						return drawBar(box.x, box.y, box.x, box.y+box.height);
					case 'right':
						return drawBar(box.x + box.width, box.y, box.x + box.width, box.y+box.height);
					}			
				} else {
					moveCache.side = null;
				}
			} else if (gridDropTargets.length == 1) {
				moveCache.container = gridDropTargets[0];
				moveCache.occupation = [];
				calculateArea(cellDragTargets);
			}
			clearBar();
		}
	}
	
}

type Push = {
	from: number,
	horiz: boolean,
	push: number
}


export function initCellDropCallback(command: Command, ) : DropCallback {
	
	function getPush(area: GridArea, to, xOrdinals: Ordinals, yOrdinals: Ordinals) : Push {
		if ((moveCache.side == 'up') || (moveCache.side=='down')) {
			const d = area.y[1]- area.y[0];
			const place = (moveCache.side == 'down') ? to.y+1 : to.y;
			to.y = place - area.y[0];
			const from = getOrdinal(place, yOrdinals);
			const dist = getOrdinal(place + d, yOrdinals) - from;
			return { from: from, horiz: false, push: dist};
		} else {
			const d = area.x[1]- area.x[0];
			const place = (moveCache.side == 'right') ? to.x+1 : to.x;
			to.x = place - area.x[0];
			const from = getOrdinal(place, xOrdinals);
			const dist = getOrdinal(place + d, xOrdinals) - from;
			return { from: from, horiz: true, push: dist};
		}
	}
	
	return function(dragState, _evt, dropTargets) {
		const dragTargets = dragState.map(s => s.dragTarget);
		const dragTargetIds = dragTargets.map(s => s.getAttribute("id"));
		const cellDropTargets = dropTargets.filter(dt => isCell(dt));
		const cellDragTargets = dragTargets.filter(dt => isCell(dt));
		const gridDropTargets = dropTargets.filter(dt => isEmptyGrid(dt));
	
		if (cellDragTargets.length != dragTargets.length) {
			// can't drop here
			return;
		}
		
		if ((cellDropTargets.length == 1) || (gridDropTargets.length == 1)) {
			// we're going to drop here.
			const cellDrop = cellDropTargets.length == 1;
			const container = moveCache.container
			const containerId = container.getAttribute("id");
			const {xOrdinals, yOrdinals} = getOrdinals(container);
			
			const dropInfo = cellDrop ? parseInfo(cellDropTargets[0]) : {};
			const dropX = cellDrop ? dropInfo['grid-x'] : [ 0, 0 ];
			const dropY = cellDrop ? dropInfo['grid-y'] : [ 0, 0 ];
			const to = {x: dropX[0], y: dropY[0] };
			
			if ((moveCache.side) && (container == moveCache.container)) {
				const { from, horiz, push } = getPush(moveCache.area, to, xOrdinals, yOrdinals);
				// console.log("Push: "+from+" "+horiz+" "+push);
				
				// TODO: fix this up
//				command.push({
//					type: 'ADLMoveCells',
//					fragmentId: containerId,
//					from: from,
//					horiz: horiz,
//					push: push,
//					excludedIds: dragTargetIds
//				})
			}
			
			dragState.forEach(ds => {
				const dt = ds.dragTarget;
				const id = dt.getAttribute("id");
				
				// move into container
				command.push({
					type: 'Move',
					to: containerId,
					moveId: id,
					from: ds.dragParentId,
					fromBefore: ds.dragBeforeId
				});
				
				if (cellDrop) {
					const item = moveCache.area.items[id];
					if (item) {
						const dragInfo = parseInfo(dt);
						const oldPositionX = dragInfo['position'][0] + " " + dragInfo['position'][1];
						const oldPositionY = dragInfo['position'][2] + " " + dragInfo['position'][3];
						
						const positionX = getOrdinal(to.x+item.dx[0],xOrdinals) + " " + getOrdinal(to.x+item.dx[1]-1,xOrdinals);
						const positionY = getOrdinal(to.y+item.dy[0],yOrdinals) + " " + getOrdinal(to.y+item.dy[1]-1,yOrdinals);
					
						// set the position of the cell
						if (oldPositionX != positionX) {
							command.push({
								type: 'ReplaceStyle',
								fragmentId:  id,
								name: '--kite9-occupies-x',
								from: oldPositionX,
								to: positionX
							});
						}
						
						if (oldPositionY != positionY) {
							command.push({
								type: 'ReplaceStyle',
								fragmentId:  id,
								name: '--kite9-occupies-y',
								from: oldPositionY,
								to: positionY
							})
						}
					}					
				}

				
			});
			
		}
	}
}





