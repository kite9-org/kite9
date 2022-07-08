import { icon, numeric, change, form } from '/github/kite9-org/kite9/client/bundles/form.js?v=v0.5'
import { hasLastSelected, parseInfo, number, createUniqueId, getContainedChildren } from '/github/kite9-org/kite9/client/bundles/api.js?v=v0.5'
import { getMainSvg } from '/github/kite9-org/kite9/client/bundles/screen.js?v=v0.5'
import { getOrdinals } from '/github/kite9-org/kite9/behaviours/grid/common-grid.js?v=v0.5'

function getMinGridSize(e) {
	const info = parseInfo(e);
	if (info['layout'] == 'GRID') {
		return info['grid-size'];
	} else {
		return [1, 1];
	}
}

function getLayout(e) {
	if (e==null) {
		return 'none';
	} else {
		var l = e.getAttribute("layout");
		l = l == null ? "none" : l;
		return l;
	}
}


export function initGridLayoutPropertySetCallback(command, cellCreator, cellSelector) {
	
	if (cellSelector == undefined) {
		cellSelector = function (e) {
			return getMainSvg().querySelectorAll("[id='" + e.getAttribute("id") + "'] > [k9-info~='connected;']");
		}
	}
	
	return function(propertyOwner, contextEvent, formEvent, contextMenu, selectedElements) {
		const layout = formEvent.target.parentElement.getAttribute("title");
		
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
					
					var newId = createUniqueId();
					var num = 0;
					for (var x = 0; x < cols; x++) {
						for (var y = 0; y < rows; y++) {
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
							containedIds: getContainedChildren(f, x => {
								var ui = x.getAttribute("k9-ui");
								return !(ui == undefined ? "" : ui).includes('orphan');
							}),
							base64Element: command.getAdl(cellId)
						});
					});
					
					const gridInfo = parseInfo(e);
					const gridSize = gridInfo['grid-size'];

					command.push({
						fragmentId: id,
						type: 'ReplaceStyle',
						name: 'kite9-grid-size',
						from: gridSize[0]+ ' ' + gridSize[1]
					});
					
					command.push({
						fragmentId: id,
						type: 'ReplaceStyle',
						name: 'kite9-grid-rows',
						from: gridSize[1]
					});
					
					command.push({
						fragmentId: id,
						type: 'ReplaceStyle',
						name: 'kite9-grid-columns',
						from: gridSize[0]
					});
				}
			} else {
				if (layout == 'grid') {
					// introduction of grid
					
					var firstCellId;
					var newId = createUniqueId();
					var num = 0;
					for (var x = 0; x < cols; x++) {
						for (var y = 0; y < rows; y++) {
							var cellId = cellCreator(id, x, y, newId+"-"+(num++));
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
						type: 'ReplaceAttr',
						name: 'layout',
						to: layout == 'none' ? null : layout,
						from: e.getAttribute('layout')
					});
				} else {
					// normal layout change, doesn't concern us
				}
			}
		});

	}
	
}

var rows = 2;
var cols = 2;

export function initGridLayoutPropertyFormCallback() {

	return function(propertyOwner, contextEvent, contextMenu, selectedElements) {
		const ls = hasLastSelected(selectedElements, true);
		const layout = getLayout(ls);
		const minGridSize = getMinGridSize(ls);
		
		var htmlElement = contextMenu.get(event);
		var hr = document.createElement("hr");
		htmlElement.appendChild(hr);
	
		rows = Math.max(2, minGridSize[1]);
		cols = Math.max(cols, minGridSize[0]);
		
		htmlElement.appendChild(form([
			change(
				numeric('Rows', rows, { 'min' : ''+minGridSize[1]}), 
				(evt) => rows = number(evt.target.value)),
			change(
				numeric('Cols', cols, { 'min' : ''+minGridSize[0]}), 
				(evt) => cols = number(evt.target.value))
			]));
		
		var img2 = contextMenu.addControl(event, "/public/behaviours/containers/layout/grid.svg","Grid", undefined);
		img2.children[0].style.borderRadius = "0px";
		img2.setAttribute("title", "grid");
		img2.addEventListener("click", (formEvent) => propertyOwner.setProperty(contextEvent, formEvent, contextMenu, selectedElements));
	}
}