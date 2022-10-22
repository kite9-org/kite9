import { getMainSvg, svg } from '/public/bundles/screen.js'
import { hasLastSelected, parseInfo, getContainingDiagram, reverseDirection, getNextSiblingId, isTerminator, isLink } from '/public/bundles/api.js'

function directionSelector() {
	return Array.from(getMainSvg().querySelectorAll("[id][k9-ui~=direction]"))
			.filter(e => isTerminator(e)); 
}

function terminatorSelector() {
	return Array.from(getMainSvg().querySelectorAll("[id][k9-info*=terminator]"))
			.filter(e => isTerminator(e)); 
}

function getDirection(e) {
	if (e==null) {
		return 'none';
	} else {
		const info = parseInfo(e);
		const l = info['direction'];
		return ((l == 'null') || (l == undefined)) ? undefined : l.toLowerCase();
	}
}

export function initDirectionContextMenuCallback(command, selector = directionSelector) {
	
	function setDirection(e, direction, contextMenu) {
		contextMenu.destroy();
		const diagramId = getContainingDiagram(e).getAttribute("id");
		const id = e.getAttribute("id")

		const alignOnly = e.classList.contains("kite9-align");
		
		if (alignOnly && (direction == 'null')) {
			command.pushAllAndPerform([{
					type: 'ADLDelete',
					fragmentId: e.getAttribute("id"),
					cascade: true
			}]);
		} else {
			if (direction == 'null') {
				// causes the attribute to be removed.
				direction = null;	
			} 
			
			command.pushAllAndPerform([{
				fragmentId: id,
				type: 'ReplaceAttr',
				name: 'drawDirection',
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
	
	function drawDirection(event, cm, direction, selected) {
		var title, src;
		
		if (direction != "null") {
			title= "Link Direction ("+direction+")";
			src = "/public/behaviours/links/direction/"+direction.toLowerCase()+".svg";
		} else {
			title =  "Link Direction (undirected)";
			src =  "/public/behaviours/links/direction/undirected.svg";				
		}

		var a = cm.addControl(event, src, title);
		var img = a.children[0];
		
		if (selected == direction) {
			img.setAttribute("class", "selected");
		}
		
		return img;
	}
	
	/**
	 * Provides a link option for the context menu
	 */
	return function(event, contextMenu) {
		
		const e = hasLastSelected(selector(), true);
		if (e) {
			const debug = parseInfo(e);
			const direction = debug.direction;
			
			if (debug.link) {
				const contradicting = debug.contradicting == "yes";
				const reverse = contradicting ? false : (debug.direction == 'LEFT' || debug.direction == 'UP');
				
				var htmlElement = contextMenu.get(event);
				const d2 = reverse ? reverseDirection(direction) : direction;
				var img = drawDirection(event, contextMenu, d2);
				if (contradicting) {
					img.style.backgroundColor = "#ff5956";
				}
				
				function handleClick() {
					contextMenu.clear(event);
					
					["null", "UP", "DOWN", "LEFT", "RIGHT"].forEach(s => {
						var img2 = drawDirection(event, contextMenu, s, d2);
						var s2 = reverse ? reverseDirection(s) : s;
						img2.addEventListener("click", () => setDirection(e, s2, contextMenu));
					});
				}
				
				img.addEventListener("click", handleClick);
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
	
	function ensureDirectionIndicator(e, direction) {		
		var indicator = e.querySelector(INDICATOR_SELECTOR);
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
