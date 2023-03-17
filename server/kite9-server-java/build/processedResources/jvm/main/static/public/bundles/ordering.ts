import { parseInfo, getContainerChildren, isConnected } from "./api.js";
import { getSVGCoords, getElementPageBBox, getMainSvg } from './screen.js'

/**
 * Works out the closest element to the point at c.  
 */
function doSort(contents: Element[], horiz : boolean, c: number) : Element | null {
	let sorted = contents
		.map((e, i) => {
			const box = getElementPageBBox(e as SVGGraphicsElement);
			const pos = horiz ? (box.x + box.width/2) : (box.y + box.height/2);
			const val = c < 0 ? (0 - c - pos) : (pos - c);
			const out = {
				p: val,
				index: i,
				e: e
			};
			return out;
		});
	
	sorted = sorted.filter(pos => (pos.p >= 0));
	sorted = sorted.sort((a, b) => a.p - b.p);

	if (sorted.length == 0) {
		return null;
	} else {
		return sorted[0].e;
	}
}

export function getBefore(container : Element, evt : Event, ignore : Element[] = []) {
	
	const info = parseInfo(container);
	const layout = info.layout;
	const pos = getSVGCoords(evt);
	
	const allChildren = getContainerChildren(container, ignore);
	const connectedChildren = allChildren.filter(c => isConnected(c));
	const otherChildren = allChildren.filter(c => !isConnected(c));
	var out = null;

	switch (layout) {
		case 'null':
		case 'right':
		case 'horizontal':
			out = doSort(connectedChildren, true, pos.x);
			break;
		case 'left':
			out = doSort(connectedChildren, true, -pos.x);
			break;
		case 'up':
			out = doSort(connectedChildren, false, -pos.y);
			break;
		case 'down':
		case 'vertical':
			out = doSort(connectedChildren, false, pos.y);
			break;
		case 'grid': {
			// just compare with elements on the current line.
			const intersectingChildren = connectedChildren
				.filter(e => {
					const box = getElementPageBBox(e);
					return (box.y <= pos.y) && (box.y + box.height >= pos.y);
				});
			
			out = doSort(intersectingChildren, true, pos.x);
			
			if (out == null) {
				const lastOnLine = intersectingChildren[intersectingChildren.length-1];
				const idx = connectedChildren.indexOf(lastOnLine);
				
				if (idx < connectedChildren.length-1) {
					out = connectedChildren[idx+1];
				}
			}
		}
	}
	
	if (out) {
		return out;
	} else if (otherChildren.length>0) {
		return otherChildren[0];
	} else {
		return null;
	}
}


export function getBeforeId(container: Element, evt: Event, ignore: Element[] = []) {
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


let bar : Element = null;
let path : Element = null;

export function clearBar() {
	if (bar != null) {
		bar.parentNode.removeChild(bar);
		bar = null;
	}
}

export function drawBar(fx: number, fy: number, tx: number, ty: number, container: Element = getMainSvg()) {
	if ((bar != null) && (bar.parentNode != container))  {
		clearBar();
	}
	
	if (bar == null) {
		bar = document.createElementNS("http://www.w3.org/2000/svg", "g");
		bar.setAttributeNS(null, 'k9-highlight', 'bar outline');
		bar.classList.add('selected');
		bar.setAttributeNS(null, 'pointer-events', 'none');
		
		path = document.createElementNS("http://www.w3.org/2000/svg", "path");
		path.setAttributeNS(null, "stroke", "blue");
		container.appendChild(bar);
		bar.appendChild(path);
	}

	path.setAttribute("d", "M" + fx + " " + fy + " L " + tx + " " + ty);
}
