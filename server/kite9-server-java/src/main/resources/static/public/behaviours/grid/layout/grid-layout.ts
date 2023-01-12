import { numeric, change } from '../../../bundles/form.js'
import { parseInfo, number, createUniqueId, getContainedChildIds, isConnected, getParentElement, onlyLastSelected, getAffordances } from '../../../bundles/api.js'
import { getOrdinals } from '../../grid/common-grid.js'
import { Command } from '../../../classes/command/command.js';
import { FormCallback, SetCallback } from '../../../classes/context-menu/property.js';
import { PaletteSelector } from '../../../bundles/types.js';

function getMinGridSize(e: Element) : [number, number] {
	const info = parseInfo(e);
	if (info['layout'] == 'GRID') {
		return info['grid-size'];
	} else {
		return [1, 1];
	}
}

function getLayout(e: Element) {
	const info = parseInfo(e);
	return info.layout;
}

export type CellCreator = (parentId: string, x: number, y: number, newId: string) => string

export function initGridLayoutPropertySetCallback(
	command: Command, 
	cellCreator: CellCreator, 
	cellSelector: PaletteSelector = undefined) : SetCallback {
	
	if (cellSelector == undefined) {
		cellSelector = function (e) {
			return Array.from(e.querySelectorAll("[id]"))
				.filter(ee => isConnected(ee))
				.filter(ee => getParentElement(ee) == e);
		}
	}
	
	return function(_propertyOwner, _contextEvent, formEvent, _contextMenu, selectedElements) {
		const layout = (formEvent.currentTarget as Element).getAttribute("title");

		Array.from(selectedElements).forEach(e => {
			const existing = getLayout(e);
			const id = e.getAttribute("id");
			
			if (existing == 'grid') {
				if (layout == 'grid') {
					// potential increase size of grid (we don't do decreases)
					
					const gridInfo = parseInfo(e);
					const gridSize = gridInfo['grid-size'];
					const ordinals = getOrdinals(e);
					const maxOrdX = ordinals.xOrdinals[ordinals.xOrdinals.length - 1];
					const maxOrdY = ordinals.yOrdinals[ordinals.yOrdinals.length - 1];
					
					const newId = createUniqueId();
					let num = 0;
					for (let x = 0; x < cols; x++) {
						for (let y = 0; y < rows; y++) {
							if ((x >= gridSize[0]) || (y >= gridSize[1])) {
								const ordX = x >= gridSize[0] ? maxOrdX + x - gridSize[0] + 1 : ordinals.xOrdinals[x];
								const ordY = y >= gridSize[1] ? maxOrdY + y - gridSize[1] + 1 : ordinals.yOrdinals[y];
								cellCreator(id, ordX, ordY, newId+"-"+(num++));
							}
						}
					}
					
				} else {
					// removal of grid
					// remove all the grid cells within
					cellSelector(e).forEach(f => {
						const cellId = f.getAttribute("id");
						command.push({
							type: 'Delete',
							fragmentId: id,
							containedIds: getContainedChildIds(f, x => {
								const ui = getAffordances(x);
								return ui.includes('orphan');
							}),
							base64Element: command.getAdl(cellId)
						});
					});
					
					const gridInfo = parseInfo(e);
					const gridSize = gridInfo['grid-size'];

					command.push({
						fragmentId: id,
						type: 'ReplaceStyle',
						name: '--kite9-grid-size',
						from: gridSize[0]+ ' ' + gridSize[1]
					});
					
					command.push({
						fragmentId: id,
						type: 'ReplaceStyle',
						name: '--kite9-grid-rows',
						from: gridSize[1]
					});
					
					command.push({
						fragmentId: id,
						type: 'ReplaceStyle',
						name: '--kite9-grid-columns',
						from: gridSize[0]
					});
				}
			} else {
				if (layout == 'grid') {
					// introduction of grid
					
					let firstCellId : string;
					const newId = createUniqueId();
					let num = 0;
					for (let x = 0; x < cols; x++) {
						for (let y = 0; y < rows; y++) {
							const cellId = cellCreator(id, x, y, newId+"-"+(num++));
							if (firstCellId == null) {
								firstCellId = cellId;
							}
						}
					}


					// move all the existing contents into the first cell
					cellSelector(e).forEach(f => {
						command.push({
							type: 'Move',
							to: firstCellId,
							moveId: f.getAttribute("id"),
							from: id
						});
					});
					
					command.push({
						fragmentId: id,
						type: 'ReplaceStyle',
						name: '--kite9-layout',
						to: layout,
						from: getLayout(e)
					});
				} else {
					// normal layout change, doesn't concern us
				}
			}
		});

	}
	
}

let rows = 2;
let cols = 2;

export function initGridLayoutPropertyFormCallback() : FormCallback {

	return function(propertyOwner, contextEvent, contextMenu, selectedElements) {
		const ls = onlyLastSelected(selectedElements);
		const minGridSize = getMinGridSize(ls);
		
		const fieldset = contextMenu.fieldset(contextEvent, "Grid");
	
		rows = Math.max(2, minGridSize[1]);
		cols = Math.max(cols, minGridSize[0]);
		
		
		
		fieldset.appendChild(
			change(
				numeric('Rows', rows, { 'min' : ''+minGridSize[1]}), 
				() => rows = number((contextEvent.target as HTMLFormElement).value)));
				
		fieldset.appendChild(
			change(
				numeric('Cols', cols, { 'min' : ''+minGridSize[0]}), 
				() => cols = number((contextEvent.target as HTMLFormElement).value)));
		
		contextMenu.addControl(contextEvent, "/public/behaviours/containers/layout/grid.svg","Grid", 
			(formEvent) => propertyOwner.setProperty(contextEvent, formEvent, contextMenu, selectedElements),
			"Grid", {"title": "grid", "style" : "border-radius: 0px; "});
	}
}