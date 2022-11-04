import { getMainSvg, svg } from '../../../bundles/screen.js'
import { parseInfo, getContainingDiagram, getNextSiblingId, isTerminator, onlyLastSelected } from '../../../bundles/api.js'
import { Command } from '../../../classes/command/command.js';
import { Selector } from '../../../bundles/types.js';
import { ContextMenu, ContextMenuCallback } from '../../../classes/context-menu/context-menu.js';
import { LinkDirection, reverseDirection } from '../linkable.js';

function directionSelector() {
	return Array.from(getMainSvg().querySelectorAll("[id][k9-ui~=direction]"))
			.filter(e => isTerminator(e)); 
}

function terminatorSelector() {
	return Array.from(getMainSvg().querySelectorAll("[id][k9-info*=terminator]"))
			.filter(e => isTerminator(e)); 
}

function getDirection(e: Element) : LinkDirection {
	if (e==null) {
		return undefined;
	} else {
		const info = parseInfo(e);
		const l = info['direction'];
		return l;
	}
}

export function initDirectionContextMenuCallback(
	command: Command, 
	selector : Selector = directionSelector) : ContextMenuCallback {
	
	function setDirection(e:Element, direction: LinkDirection, contextMenu: ContextMenu) {
	
		contextMenu.destroy();
		const diagramId = getContainingDiagram(e).getAttribute("id");
		const id = e.getAttribute("id")

		const alignOnly = e.classList.contains("kite9-align");
		
		if (alignOnly && (direction == undefined)) {
			command.pushAllAndPerform([{
					type: 'Delete',
					fragmentId: e.getAttribute("id"),
					cascade: true
			}]);
		} else {
			command.pushAllAndPerform([{
				fragmentId: id,
				type: 'ReplaceStyle',
				name: '--kite9-direction',
				to: direction,
				from: e.getAttribute('drawDirection')
			},{
				type: 'Move',
				from: diagramId,
				fromBefore: getNextSiblingId(e),
				moveId: id,
				to: diagramId
			}]);
		}
	}
	
	function drawDirection(
		event: Event, 
		cm: ContextMenu, 
		direction: LinkDirection, 
		selected: LinkDirection = undefined) : HTMLImageElement {
		let title: string, src: string;
		
		if (direction != undefined) {
			title= "Link Direction ("+direction+")";
			src = "/public/behaviours/links/direction/"+direction.toLowerCase()+".svg";
		} else {
			title =  "Link Direction (undirected)";
			src =  "/public/behaviours/links/direction/undirected.svg";				
		}

		const a = cm.addControl(event, src, title) as HTMLDivElement; 
		const img = a.children[0] as HTMLImageElement;
		
		if (selected == direction) {
			img.setAttribute("class", "selected");
		}
		
		return img;
	}
	
	/**
	 * Provides a link option for the context menu
	 */
	return function(event, contextMenu) {
		
		const e = onlyLastSelected(selector());
		if (e) {
			const debug = parseInfo(e);
			const direction = debug.direction;
			
			if (debug.link) {
				const contradicting = debug.contradicting == "yes";
				const reverse = contradicting ? false : (debug.direction == 'left' || debug.direction == 'up');
				
				const d2 = reverse ? reverseDirection(direction) : direction;
				const img = drawDirection(event, contextMenu, d2);
				if (contradicting) {
					img.style.backgroundColor = "#ff5956";
				}
				
				img.addEventListener("click", () => {
					contextMenu.clear();
					
					["null", "up", "down", "left", "right"].forEach((s : LinkDirection) => {
						const img2 = drawDirection(event, contextMenu, s, d2);
						const s2 = reverse ? reverseDirection(s) : s;
						img2.addEventListener("click", () => setDirection(e, s2, contextMenu));
					});
				});
			}
		}
	};
}

export function initTerminatorDirectionIndicator(selector = terminatorSelector) {
	
	
	const INDICATOR_SELECTOR = ":scope > g.k9-direction";
	
	const drawingFunctions = {
		"up" : () => svg("polygon", {"points" : "-10 12, 0 -8, 10 12"}),
		"down": () => svg("polygon", {"points" : "10 -12, 0 8, -10 -12"}),
		"left": () => svg("polygon", {"points" : "12 -10, -8 0, 12 10"}),
		"right":  () => svg("polygon", {"points" : "-12 -10, 8 0, -12 10"})
	}  
	
	const noneFunction = () => svg("ellipse", {"cx" : "0", "cy": 0, "rx": 8, "ry": 8});
	
	function ensureDirectionIndicator(e: Element, direction: LinkDirection) {		
		let indicator = e.querySelector(INDICATOR_SELECTOR);
		if ((indicator != null) && (indicator.getAttribute("direction")!=direction)) {
			e.removeChild(indicator);
		} else if (indicator != null) {
			return;
		} 
		
		indicator = svg('g', {
			'class' : 'k9-direction',
			'k9-highlight' : 'fill',
			'direction' : direction,
		}, [ direction ? drawingFunctions[direction]() : noneFunction() ]);
		
		e.appendChild(indicator)
	}
	
	window.addEventListener('DOMContentLoaded', function() {
		selector().forEach(function(v) {
			const direction = getDirection(v);
			ensureDirectionIndicator(v, direction)
		})
	})
}
