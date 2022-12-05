import { hasLastSelected, parseInfo, createUniqueId, getContainedChildIds } from '../../../bundles/api.js'
import { nextOrdinal, getOrdinals  } from '../../../behaviours/grid/common-grid.js' 
import { getMainSvg } from '../../../bundles/screen.js';
import { Command } from '../../../classes/command/command.js';
import { Direction, directions, Selector } from '../../../bundles/types.js';
import { ContextMenu, ContextMenuCallback } from '../../../classes/context-menu/context-menu.js';


export function initCellAppendContextMenuCallback(
	command: Command, 
	selector: Selector = undefined) : ContextMenuCallback {
	
	if (selector == undefined) {
		selector = function() {
			return Array.from(getMainSvg().querySelectorAll("[id][k9-palette~='cell'].selected"))
		}
	}
	
	function doAppend(container: Element, selectedElements: Element[], side: Direction) {
		const { xOrdinals, yOrdinals } = getOrdinals(container);
		
		const ordinalChangeMap : { [ index : number] : number } = {};
		const ordinalItems = {};
		const itemPositions = {}; 
		let horiz = false;
		
		selectedElements.forEach(e => {
			const info = parseInfo(e);
			const position = info['position'];
			itemPositions[e.getAttribute("id")] = position;
			let pos; 
			switch (side) {
			case 'up':
				pos = position[2];
				break;
			case 'down':
				pos = nextOrdinal(position[3], yOrdinals);
				break;
			case 'left':
				pos = position[0];
				horiz = true;
				break;
			case 'right':
				pos = nextOrdinal(position[1], xOrdinals);
				horiz = true;
				break;			
			}
			ordinalChangeMap[pos] = pos;
			ordinalItems[pos] = ordinalItems[pos] ? [ ...ordinalItems[pos], e ] : [ e ];
		})
		
		// 2. Perform move operations to make space with
		const keys : number[] = Object.keys(ordinalChangeMap).map(s => parseInt(s))
		const order = keys.sort((a,b) => a-b);
		const ordinals = horiz ? xOrdinals : yOrdinals;
		
		order.forEach(o => {
//			const from = ordinalChangeMap[o];
			const containerId = container.getAttribute("id");

			// first, move the other ordinals down.
			const position = ordinalChangeMap[o];
			const change = nextOrdinal(ordinalChangeMap[o], ordinals) - ordinalChangeMap[o];
			order.forEach(o2 =>  { 
				if (o2 >= o) { 
					ordinalChangeMap[o2] = ordinalChangeMap[o2] + change;
				}
			});
			
			// apply the move command on the server
			// TODO: replace this with JS
//			command.push({
//				type: 'ADLMoveCells',
//				fragmentId: containerId,
//				from: from,
//				horiz: horiz,
//				push: change
//			})
			
			// now, introduce a line of cells in position
			ordinalItems[o].forEach(item => {
				
				const newId = createUniqueId();
				const itemId = item.getAttribute("id");
				
				command.push({
					type: 'InsertXML',   
					fragmentId: containerId,
					base64Element: command.getAdl(itemId),
					containedIds: getContainedChildIds(item),
					newId: newId,
				})
				
				const itemPos = itemPositions[itemId];
				const newPos = [
					horiz ? position : itemPos[0],
					horiz ? position : itemPos[1],
					!horiz ? position : itemPos[2],
					!horiz ? position : itemPos[3]
				];
				
				// set the position of the cell
				command.push({
					type: 'ReplaceStyle',
					fragmentId:  newId,
					name: '--kite9-occupies',
					to: newPos[0] + ' ' + newPos[1] + ' ' + newPos[2] + ' ' + newPos[3],
					from: item.style['--kite9-occupies']
				})
			});
		})
	}
	
	function appendsCells(e: Element[], s: Direction, cm: ContextMenu) {
		cm.destroy();
		const parents = e.map(i => i.parentElement);
		const containers = [...new Set(parents)];
		containers.forEach(c => {
			doAppend(c, e.filter(i => i.parentElement == c), s);			
		})
		command.perform();
	}
	
	
	/**
	 * Provides overlays for adding cells
	 */
	return function(event, cm) {
		
		const e = hasLastSelected(selector());
		
		if (e.length > 0) {
			
			const handleClick = () => {
				// remove the other stuff from the context menu
				cm.clear();

				directions.forEach(s => {
					cm.addControl(event, "/public/behaviours/grid/append/" + s.toLowerCase() + ".svg", 
							"Append (" + s + ")", () => appendsCells(selector(), s, cm));
				});
			}
			
			cm.addControl(event, "/public/behaviours/grid/append/append.svg", 'Append', handleClick);
		}
	}
	
	
}