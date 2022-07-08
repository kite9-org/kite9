import { getSVGCoords, getElementPageBBox, getMainSvg } from '/public/client/bundles/screen.js' ;
import { createUniqueId, parseInfo, onlyUnique, changeId } from '/github/kite9-org/kite9/client/bundles/api.js?v=v0.5' 

/**
 * Provides functionality for drawing links on the diagram, and keeping track of them.
 */
export class Linker {
	 
	constructor(animate, getLinkTarget) {
		this.svg = getMainSvg();
		this.drawing = [];
		this.animate = animate;
		
		this.callbacks = [];
		
		if (getLinkTarget == undefined) {
			this.getLinkTarget = function(v) {
				if (v.hasAttribute("k9-ui") && v.hasAttribute("id")) {
					if (v.getAttribute("k9-ui").includes('connect')) {
						return v;
					} else {
						return null;
					}
				} else if (v == getMainSvg()) {
					return null;
				} else {
					return this.getLinkTarget(v.parentNode);
				}
			}
		} else {
			this.getLinkTarget = getLinkTarget;
		}
		
	}
	
	add(cb) {
		// nb. additions go to the start
		this.callbacks.unshift(cb);
	}
	
	start(selectedElements, template) {
		if (template == undefined) {
			alert("Link Template Not Loaded");
			return;
		}
		
		Array.from(selectedElements)
			.filter(onlyUnique)
			.forEach(e => {
				var newLink = template.cloneNode(true);
				this.svg.appendChild(newLink);
				newLink.setAttribute("temp-from", e.getAttribute("id"));
				const newId = createUniqueId();
				changeId(newLink, template.getAttribute("id"), newId);
				newLink.classList.remove("selected");
	
				const bbox = getElementPageBBox(e)
				var from = { x: bbox.x + bbox.width/2, y: bbox.y + bbox.height/2 };
				this.drawing.push(newLink);
				newLink.start = from;
				newLink.setAttributeNS(null, 'pointer-events', 'none');
				this.animate(newLink, from, this.mouseCoords);
			});
	}
	
	move(evt) {
		this.mouseCoords = getSVGCoords(evt);
		this.drawing.forEach(e => {
			this.animate(e, e.start, this.mouseCoords);
		});
		
	}
	
	moveCoords(x1, y1, x2, y2) {
		this.drawing.forEach(e => {
			this.animate(e, {x: x1, y: y1}, {x: x2, y: y2});
		});
	}

	removeDrawingLinks() {
		this.drawing.forEach(e => {
			e.parentElement.removeChild(e);
		});
		this.drawing = [];
	}

	end(evt, perform=true) {
		if (this.drawing.length == 0) {
			return;
		}
		
		this.callbacks.forEach(cb => cb(this, evt, perform));
		
		//evt.stopPropagation();
	}
	
	get() {
		return this.drawing;
	}
	
	clear() {
		this.drawing = [];
	}
}