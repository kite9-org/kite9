import { getHtmlCoords, getMainSvg, getSVGCoords } from '/public/bundles/screen.js'
import { ensureCss } from '/public/bundles/ensure.js'



/**
 * This allows us to draw extra controls around the edge of a diagram element, which you can click or drag.
 */
export class Overlay {
	
	constructor(cb) {
		ensureCss('/public/classes/overlay/overlay.css');	
		this.sizing = null;	
	}
	
	/**
	 * Removes the overlay from the screen.
	 */
	destroy() {
		document.querySelectorAll("g._overlay").forEach(e => e.parentElement.removeChild(e));
	}

	ensureOverlay() {
		const _this = this;
		var controlContainer = getMainSvg().querySelector("g._overlay");
		if (!controlContainer) {
			controlContainer = document.createElementNS("http://www.w3.org/2000/svg", "g");
			getMainSvg().appendChild(controlContainer);
			controlContainer.setAttribute("class", "_overlay");
			const background = document.createElementNS("http://www.w3.org/2000/svg", "rect");
			background.setAttribute('width', getMainSvg().getAttribute("width"));
			background.setAttribute('height', getMainSvg().getAttribute("height"));
			controlContainer.appendChild(background);

			const defs = document.createElementNS("http://www.w3.org/2000/svg", "defs");
			controlContainer.appendChild(defs);
			const marker = document.createElementNS("http://www.w3.org/2000/svg", "marker");
			marker.setAttribute("id", "k9-sizing-arrow");
			marker.setAttribute("markerHeight", "80px");
			marker.setAttribute("markerWidth", "80px");
			marker.setAttribute("orient","auto-start-reverse");
			marker.setAttribute("refX","80px");
			marker.setAttribute("refY","40px");

			
			defs.appendChild(marker);
			const line = document.createElementNS("http://www.w3.org/2000/svg", "path");
			line.setAttribute("d", "M 80 0 V80 M80 40 L60 20 M 80 40 L 60 60");
			marker.appendChild(line);
			
			function endSize(e) {
				_this.sizing = null;
				e.stopPropagation();
			}
			
			// event for when sizing handle is being moved
			function moveSize(e) {
				if (_this.sizing) {
					_this.sizing(e);
				}
			}
				
			background.addEventListener("mouseup", endSize);
			background.addEventListener("touchend", endSize);
			background.addEventListener("touchmove", moveSize);
			background.addEventListener("mousemove", moveSize); 
		}
		
		return controlContainer;
	}

	createSizingArrow(target, fx, fy, length, horiz, inverse, cb) {
		const _this = this;
		var controlContainer = this.ensureOverlay();
		
		var line = document.createElementNS("http://www.w3.org/2000/svg", "path")
		
		var handle = document.createElementNS("http://www.w3.org/2000/svg", "ellipse")
		handle.setAttribute("rx", "8px");
		handle.setAttribute("ry", "8px");
		controlContainer.appendChild(line);
		controlContainer.appendChild(handle);
		
		var initial = length;
		
		function setSize(length) {
			length = parseFloat(length)
			if (isNaN(length)) {
				length = 0;
			}
			const start = "M"+fx+" "+fy;
			const ex = horiz ? (inverse ? fx - length : fx + length) :  fx;
			const ey = horiz ? fy : (inverse ? fy - length : fy + length);
			const end = " L"+ex+" "+ey;
			line.setAttribute("d",start + end);
			handle.setAttribute("cx", ex);
			handle.setAttribute("cy", ey);
			if (length > 0) {
				line.setAttribute("marker-start","url(#k9-sizing-arrow)");
				line.setAttribute("marker-end","url(#k9-sizing-arrow)");
			} else {
				handle.removeAttribute("marker-end");
				handle.removeAttribute("marker-start");
			}
		}
		
		function move(e) {
			const newCoords = getSVGCoords(e);	
			const delta = { x: newCoords.x - initial.x, y: newCoords.y - initial.y}
			const newLength = Math.round(horiz ? ( inverse ? length - delta.x : length + delta.x) : 	
								      ( inverse ? length - delta.y : length + delta.y));
			if (newLength >= 0) {
				initial = newCoords;
				setSize(newLength);
				cb(newLength);
				length = newLength;
			} else {
				setSize(0);
				cb(0);
				length = 0;
			}
		}
		
		// handle events for when someone starts to drag the sizing handle
		function startSize(e) {
			_this.sizing = move
			initial = getSVGCoords(e);
			
			e.stopPropagation();
		}
		
		handle.addEventListener("touchstart", startSize);
		handle.addEventListener("mousedown", startSize);
		
		setSize(length);
	
		return setSize;
	}
}
