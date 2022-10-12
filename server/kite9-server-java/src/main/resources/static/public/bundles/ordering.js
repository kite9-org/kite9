import { parseInfo, getContainerChildren } from "/public/bundles/api.js";
import { getSVGCoords, getElementPageBBox, getMainSvg } from '/public/bundles/screen.js'

function doSort(contents, horiz, c, ignore) {
	var sorted = contents
		.map((e, i) => {
			const box = getElementPageBBox(e);
			const pos = horiz ? (box.x + box.width/2) : (box.y + box.height/2);
			const val = c < 0 ? (0 - c - pos) : (pos - c);
			const out = {
				p: val,
				connected: e.getAttribute("k9-info").includes("rectangular: connected;"),
				index: i,
				e: e
			};
			return out;
		});
	
	sorted = sorted.filter(pos => (pos.p >= 0) || (!pos.connected));
	sorted = sorted.sort((a, b) => (!a.connected || !b.connected) ? a.index - b.index : a.p - b.p);

	if (sorted.length == 0) {
		return null;
	} else {
		return sorted[0].e;
	}
}

export function getBefore(container, evt, ignore) {
	
	const info = parseInfo(container);
	const layout = info.layout;
	const pos = getSVGCoords(evt);
	
	const allChildren = getContainerChildren(container, ignore);

	switch (layout) {
		case 'null':
		case 'RIGHT':
		case 'HORIZONTAL':
			return doSort(allChildren, true, pos.x, ignore);
		case 'LEFT':
			return doSort(allChildren, true, -pos.x, ignore);
		case 'UP':
			return doSort(allChildren, false, -pos.y, ignore);
		case 'DOWN':
		case 'VERTICAL':
			return doSort(allChildren, false, pos.y, ignore);
		case 'GRID': 
			// just compare with elements on the current line.
			const intersectingChildren = allChildren
				.filter(e => {
					const box = getElementPageBBox(e);
					return (box.y <= pos.y) && (box.y + box.height >= pos.y);
				});
			
			const out = doSort(intersectingChildren, true, pos.x, ignore);
			
			if (out == null) {
				const lastOnLine = intersectingChildren[intersectingChildren.length-1];
				const idx = allChildren.indexOf(lastOnLine);
				
				if (idx < allChildren.length-1) {
					return allChildren[idx+1];
				}
			} else {
				return out;
			}
			
		default:
			return null;
	}
}


export function getBeforeId(container, evt, ignore) {
	if (evt == undefined) {
		return undefined;
	}
	
	const before = getBefore(container, evt, ignore);
	if (before == null) {
		return null;
	} else {
		return before.getAttribute("id");
	}
}


var bar = null;
var path = null;

export function clearBar() {
	if (bar != null) {
		bar.parentNode.removeChild(bar);
		bar = null;
	}
}

export function drawBar(fx, fy, tx, ty, container) {
	if ((bar != null) && (bar.parentNmde != container))  {
		clearBar();
	}
	
	if (bar == null) {
		var svg = container == undefined ? getMainSvg() : container;
		bar = document.createElementNS("http://www.w3.org/2000/svg", "g");
		bar.setAttributeNS(null, 'k9-highlight', 'bar outline');
		bar.classList.add('selected');
		bar.setAttributeNS(null, 'pointer-events', 'none');
		
		path = document.createElementNS("http://www.w3.org/2000/svg", "path");
		path.setAttributeNS(null, "stroke", "blue");
		svg.appendChild(bar);
		bar.appendChild(path);
	}

	path.setAttribute("d", "M" + fx + " " + fy + " L " + tx + " " + ty);
}
