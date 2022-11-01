import { getMainSvg, getSVGCoords, svg } from '../../bundles/screen.js'
import { ensureCss } from '../../bundles/ensure.js'

/**
 * This allows us to draw extra controls around the edge of a diagram element, which you can click or drag.
 */
export class Overlay {

	sizing?: (e: Event) => void
	
	constructor() {
		ensureCss('/public/classes/overlay/overlay.css');	
		this.sizing = null;	
	}
	
	/**
	 * Removes the overlay from the screen.
	 */
	destroy() : void {
		document.querySelectorAll("g._overlay").forEach(e => e.parentElement.removeChild(e));
	}

	ensureOverlay() : Element {
//		const _this = this;
		
		function endSize(o: Overlay, e : Event) {
			o.sizing = null;
			e.stopPropagation();
		}
			
		// event for when sizing handle is being moved
		function moveSize(o: Overlay, e: Event) {
			if (o.sizing) {
				o.sizing(e);
			}
		}
				
		let controlContainer = getMainSvg().querySelector("g._overlay");
		if (!controlContainer) {
			controlContainer = svg("g", {"class" : "_overlay"}, [
				svg("rect", {
					'width': getMainSvg().getAttribute("width"),
					'height': getMainSvg().getAttribute("height"),
					"class": "_background"}),
				svg("defs", {}, [
					svg("marker", {
							"id": "k9-sizing-arrow",
							"markerHeight": "80px",
							"markerWidth": "80px",
							"orient": "auto-start-reverse",
							"refX":"80px",
							"refY":"40px"
						},[
							svg("path", {"d": "M80 40 L60 20 M 80 40 L 60 60", "class" : "_sizing-marker"})
						])
					])
				]);
			

			controlContainer.addEventListener("mouseup", (e) => endSize(this, e));
			controlContainer.addEventListener("touchend", (e) => endSize(this, e));
			controlContainer.addEventListener("touchmove", (e) => moveSize(this, e));
			controlContainer.addEventListener("mousemove", (e) => moveSize(this, e)); 
			getMainSvg().appendChild(controlContainer);
		}
		
		return controlContainer;
	}
	
	createSizingRect(x: number, y: number, width: number, height: number,
		_up: number, _right: number, _down: number, _left: number) {
		const controlContainer = this.ensureOverlay();

		const rect = svg("rect", { "class": "_sizing" });

		controlContainer.appendChild(rect);

		function setSize(up: number, right: number, down: number, left: number) {
			rect.setAttribute("x", (x - left) + "px");
			rect.setAttribute("y", (y - up) + "px");
			rect.setAttribute("width", (width + left + right) + "px");
			rect.setAttribute("height", (height + up + down) + "px");
			_up = up;
			_down = down;
			_left = left;
			_right = right;
		}

		setSize(_up, _right, _down, _left);

		return [
			(up: number) => setSize(up, _right, _down, _left),
			(right: number) => setSize(_up, right, _down, _left),
			(down: number) => setSize(_up, _right, down, _left),
			(left: number) => setSize(_up, _right, _down, left)
		];
	}

	createSizingArrow(fx: number, fy: number, length: number, horiz: boolean, inverse: boolean, cb: (l: number) => void) {
		const controlContainer = this.ensureOverlay();
		
		const line = svg("path", {"class": "_sizing",
			"marker-start": "url(#k9-sizing-arrow)",
			"marker-end": "url(#k9-sizing-arrow)"})
		
		const handle = svg("ellipse", {
			"rx": "8px",
			"ry": "8px"
		})

		controlContainer.appendChild(line);
		controlContainer.appendChild(handle);
		
		let initial = {x : 0, y: 0};
		
		function setSize(l: number) {
			if (isNaN(length)) {
				length = 0;
			}
			const start = "M"+fx+" "+fy;
			const ex = horiz ? (inverse ? fx - length : fx + length) :  fx;
			const ey = horiz ? fy : (inverse ? fy - length : fy + length);
			const end = " L"+ex+" "+ey;
			line.setAttribute("d",start + end);
			handle.setAttribute("cx", "" + ex);
			handle.setAttribute("cy", "" + ey);
			initial = {x : ex, y: ey };
		}
		
		function move(e : Event) {
			const newCoords = getSVGCoords(e);
			const delta = { x: newCoords.x - initial.x, y: newCoords.y - initial.y }
			const newLength = Math.round(horiz ? (inverse ? length - delta.x : length + delta.x) :
				(inverse ? length - delta.y : length + delta.y));
			if (newLength >= 0) {
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
		function startSize(o: Overlay, e: Event) {
			o.sizing = move
			initial = getSVGCoords(e);
			
			e.stopPropagation();
		}
		
		handle.addEventListener("touchstart", (e) => startSize(this, e));
		handle.addEventListener("mousedown", (e) => startSize(this, e));
		
		setSize(length);
	
		return setSize;
	}
}
