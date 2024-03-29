import { getSVGCoords, getElementPageBBox, getMainSvg } from '../../bundles/screen.js' ;
import { createUniqueId, onlyUnique, changeId } from '../../bundles/api.js' 
import { Point } from '../../bundles/types.js';

export type Animate = (e: Element, x: Point, y: Point) => void
export type GetLinkTarget = (v: Element) => Element
export type LinkerCallback = (l: Linker, e: Event, perform: boolean) => void

export interface ElementWithStart extends Element {
	start: Point
}

/**
 * Provides functionality for drawing links on the diagram, and keeping track of them.
 */
export class Linker {

	svg = getMainSvg();
	drawing = [];
	animate : Animate;
	getLinkTarget : GetLinkTarget;
	callbacks : LinkerCallback[] = [];
	mouseCoords : Point

	constructor(animate : Animate, getLinkTarget: GetLinkTarget = undefined) {
		this.animate = animate;
		
		if (getLinkTarget == undefined) {
			this.getLinkTarget = function(v : Element) {
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
	
	add(cb : LinkerCallback) {
		// nb. additions go to the start
		this.callbacks.unshift(cb);
	}
	
	start(selectedElements : Element[], template: Element) {
		if (template == undefined) {
			alert("Link Template Not Loaded");
			return; 
		}
		
		Array.from(selectedElements)
			.filter(onlyUnique)
			.forEach(e => {
				const newLink = template.cloneNode(true) as Element;
				this.svg.appendChild(newLink);
				newLink.setAttribute("temp-from", e.getAttribute("id"));
				const newId = createUniqueId();
				changeId(newLink, template.getAttribute("id"), newId);
				newLink.classList.remove("selected");
				newLink.removeAttribute("default");
	
				const bbox = getElementPageBBox(e)
				const from = { x: bbox.x + bbox.width/2, y: bbox.y + bbox.height/2 };
				this.drawing.push(newLink);
				// not sure this is really allowed, but works in js 
				(newLink as ElementWithStart).start = from;
				newLink.setAttributeNS(null, 'pointer-events', 'none');
				this.animate(newLink, from, this.mouseCoords);
			});
	}
	
	move(evt : Event) {
		this.mouseCoords = getSVGCoords(evt);
		this.drawing.forEach(e => {
			this.animate(e, (e as ElementWithStart).start, this.mouseCoords);
		});
		
	}
	
	moveCoords(x1: number, y1: number, x2: number, y2: number) {
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

	end(evt : Event, perform=true) {
		if (this.drawing.length == 0) {
			return;
		}
		
		this.callbacks.forEach(cb => cb(this, evt, perform));
		
		//evt.stopPropagation();
	}
	
	get() {
		return this.drawing;
	}
}