import { hasLastSelected, getParentElement, parseInfo, createUniqueId, getContainedChildren } from '/github/kite9-org/kite9/client/bundles/api.js?v=v0.9'
import { nextOrdinal, getOrdinals  } from '/github/kite9-org/kite9/client/behaviours/grid/common-grid.js?v=v0.9' 


export function initCellAppendContextMenuCallback(command, selector) {
	
	if (selector == undefined) {
		selector = function() {
			return document.querySelectorAll("[id][k9-palette~='cell'].selected")
		}
	}
	
	function doAppend(container, selectedElements, side) {
		const { xOrdinals, yOrdinals } = getOrdinals(container);
		const lastSelected = hasLastSelected(selectedElements, true);
		
		var ordinalChangeMap = {};
		var ordinalItems = {};
		var itemPositions = {}; 
		var horiz = false;
		
		selectedElements.forEach(e => {
			const info = parseInfo(e);
			const position = info['position'];
			itemPositions[e.getAttribute("id")] = position;
			var pos; 
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
		const order = Object.keys(ordinalChangeMap).sort((a,b) => a-b);
		const ordinals = horiz ? xOrdinals : yOrdinals;
		
		order.forEach(o => {
			const from = ordinalChangeMap[o];
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
			command.push({
				type: 'ADLMoveCells',
				fragmentId: containerId,
				from: from,
				horiz: horiz,
				push: change
			})
			
			// now, introduce a line of cells in position
			ordinalItems[o].forEach(item => {
				
				const newId = createUniqueId();
				const itemId = item.getAttribute("id");
				
				command.push({
					type: 'InsertXML',   
					fragmentId: containerId,
					base64Element: command.getAdl(itemId),
					containedIds: getContainedChildren(item),
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
					name: 'kite9-occupies',
					to: newPos[0] + ' ' + newPos[1] + ' ' + newPos[2] + ' ' + newPos[3],
					from: item.style['kite9-occupies']
				})
			});
		})
	}
	
	function appendsCells(e, s, cm) {
		cm.destroy();
		e = Array.from(e);
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
			var htmlElement = cm.get(event);
			
			function handleClick() {
				// remove the other stuff from the context menu
				cm.clear(event);

				["right", "down", "left", "up"].forEach(s => {
					cm.addControl(event, "/github/kite9-org/kite9/client/behaviours/grid/append/" + s.toLowerCase() + ".svg", 
							"Append (" + s + ")", () => appendsCells(selector(), s, cm));
				});
			}
			
			cm.addControl(event, "/github/kite9-org/kite9/client/behaviours/grid/append/append.svg", 'Append', () => handleClick());
		}
	}
	
	
}