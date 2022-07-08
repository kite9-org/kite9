import { getHtmlCoords } from '/github/kite9-org/kite9/client/bundles/screen.js?v=v0.4'
import { ensureCss } from '/github/kite9-org/kite9/client/bundles/ensure.js?v=v0.4'
import { icon } from '/github/kite9-org/kite9/client/bundles/form.js?v=v0.4'
import { number } from '/github/kite9-org/kite9/client/bundles/api.js?v=v0.4'

/**
 * Provides functionality for populating the context menu.  Takes a number of callbacks
 * that provide functionality when the user asks the context menu to appear.
 */
export class ContextMenu {

	constructor(instrumentation) {
		this.instrumentation = instrumentation;
		this.callbacks = [];
		ensureCss('/public/client/classes/context-menu/context-menu.css?v=v0.4');
		
		this.moving = undefined;
		this.offsetCoords = undefined;
		this.eventCoords = undefined;
	
		document.addEventListener("mouseup", (e) => this.moving = undefined);
		document.addEventListener("mousemove", (e) => this.move(e));
		document.addEventListener("touchend", (e) => this.moving = undefined);
		document.addEventListener("touchmove", (e) => this.move(e), { passive: false });
		
	
	}
	
	move(e) {
		if (this.moving) {
			var ec = getHtmlCoords(e)
			this.moving.style.left = (ec.x + this.offsetCoords.x) +"px";
			this.moving.style.top = (ec.y + this.offsetCoords.y)+"px";
			e.stopPropagation();
			e.preventDefault();
			return false;
		}
	}
	
	start(e) {
		if (e.target == this.menu) {
			this.eventCoords = getHtmlCoords(e)
			this.offsetCoords = {
				x: number(this.menu.style.left) - this.eventCoords.x,
				y: number(this.menu.style.top) - this.eventCoords.y 
			}; 
			this.moving = this.menu;
		}
	}

	end(e) {
		if (this.moving) {
			var cc = getHtmlCoords(e);
			if ((this.eventCoords.x == cc.x) && (this.eventCoords.y == cc.y)) {
				this.destroy();
				e.stopPropagation();
			}					
		}
	}
	
	add(cb) {
		this.callbacks.push(cb);
	}
	
	/**
	 * Creates the context menu within the main svg element,
	 * positioning it relative to the event that created it.
	 */
	get(event) {
		var ctxMenu = document.querySelector("#contextMenu");
		if (ctxMenu) {
			return ctxMenu;
		} else {
			ctxMenu = document.createElement("div");
			ctxMenu.setAttribute("id", "contextMenu");
			ctxMenu.setAttribute("class", "contextMenu");
			ctxMenu.setAttribute("draggable", "false");

			const coords = getHtmlCoords(event);
			coords.x += 15;
			coords.y -= 20; 

			ctxMenu.style.left = coords.x +"px";
			ctxMenu.style.top = coords.y+"px";
	
			ctxMenu.addEventListener("mousedown", (e) => this.start(e)); 
			ctxMenu.addEventListener("mouseup", (e) => this.end(e)); 
			ctxMenu.addEventListener("touchstart", (e) => this.start(e)); 
			ctxMenu.addEventListener("touchend", (e) => this.end(e)); 
			
			document.querySelector("body").appendChild(ctxMenu);
			this.menu = ctxMenu;
			return ctxMenu;
		}
	}
	
	/**
	 * Call this when the user clicks on an element that might need a context menu.
	 */
	handle(event) {
		this.callbacks.forEach(cb => cb(event, this));	
	}
	
	/**
	 * Removes the context menu from the screen.
	 */
	destroy() {
		const ctxMenu = document.querySelector("#contextMenu");
		if (ctxMenu) {
			ctxMenu.parentElement.removeChild(ctxMenu);
		}
	}

	addControl(event, imageUrl, title, clickListener) {
		var htmlElement = this.get(event);
		var out = icon('_cm-'+title, title, imageUrl, clickListener)
		htmlElement.appendChild(out);
		
		const row = Math.min(htmlElement.children.length, 5);
		htmlElement.style.width = (1.5 + 4.4 * row) + 'rem';
		
		return out;
	}
	
	/**
	 * Removes all the content from the context menu
	 */
	clear(event) {
		var htmlElement = this.get(event);
		Array.from(htmlElement.children).forEach(e => {
			htmlElement.removeChild(e);
		});
		
		htmlElement.style.width = '';
	}

}
