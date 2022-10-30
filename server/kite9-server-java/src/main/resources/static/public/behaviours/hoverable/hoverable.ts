import { getMainSvg, currentTarget } from '../../bundles/screen.js'
import { getKite9Target } from '../../bundles/api.js'
import { Selector } from '../../bundles/types.js';

export class Hover {

	selectedElement: Element | null;
	
	constructor() {
		this.selectedElement = null;
		
	}

	hover(v: Element) {
		if (v != this.selectedElement) {
			v.classList.add("mouseover");
				
			if (this.selectedElement != undefined) {
				this.selectedElement.classList.remove("mouseover");
			}
	
			this.selectedElement = v;
		}
	}

	unhover(v : Element) {
		v.classList.remove("mouseover");
		if (this.selectedElement == v) {
			this.selectedElement = undefined;
		}
	}

	isHover(v : Element) : boolean {
		return v.classList.contains("mouseover")
	}
}

/**
 * Adds hoverable behaviour
 */
export function initHoverable(selector: Selector, allowed : (v: Element) => boolean, ctx = new Hover()) {
	
	if (allowed == undefined) {
		allowed = function(v) {
			return v!=undefined;
		}
	}
	
	function mouseover(event: Event) {
		if ((event as any).handledHover) {
			return;
		}
		
		const v = getKite9Target(currentTarget(event));
		
		if ((v != null) && (allowed(v))) {
			ctx.hover(v);
		
			// prevents parent elements from highlighting too.
			(event as any).handledHover = true;	
		}
	}

	function mouseout(event: Event) {
		const v = getKite9Target(currentTarget(event));
		if (allowed(v)) {
			ctx.unhover(v);
		}
	}
	
	if (selector == undefined) {
		selector = function() { 
			return Array.from(getMainSvg().querySelectorAll("[id][k9-elem]"));
		}
	}
	
	window.addEventListener('DOMContentLoaded', function() {
		selector().forEach(function(v) {
			v.removeEventListener("mouseover", mouseover);
			v.addEventListener("mouseover", mouseover);

			v.removeEventListener("mouseout", mouseout);
			v.addEventListener("mouseout", mouseout);

			v.removeEventListener("touchmove", mouseover);
			v.addEventListener("touchmove", mouseover, { passive: false });

		})
	})
}





