import { getHtmlCoords } from '../../bundles/screen.js'
import { ensureCss } from '../../bundles/ensure.js'
import { icon, fieldset, form } from '../../bundles/form.js'
import { number } from '../../bundles/api.js'

type coords = {
	x: number,
	y: number
}

type callback = (e: Event, cm: ContextMenu) => void

/**
 * Provides functionality for populating the context menu.  Takes a number of callbacks
 * that provide functionality when the user asks the context menu to appear.
 */
export class ContextMenu {

	menu : HTMLElement | null = undefined
	moving: HTMLElement | null = undefined
	callbacks : callback[] = []
	offsetCoords: coords | null = undefined
	eventCoords: coords | null = undefined

	constructor() {
		ensureCss('/public/classes/context-menu/context-menu.css');
		
		this.eventCoords = undefined;
	
		document.addEventListener("mouseup", () => this.moving = undefined);
		document.addEventListener("mousemove", (e) => this.move(e));
		document.addEventListener("touchend", () => this.moving = undefined);
		document.addEventListener("touchmove", (e) => this.move(e), { passive: false });
	}
	
	move(e : Event) {
		if (this.moving) {
			const ec = getHtmlCoords(e)
			this.moving.style.left = (ec.x + this.offsetCoords.x) +"px";
			this.moving.style.top = (ec.y + this.offsetCoords.y)+"px";
			e.stopPropagation();
			e.preventDefault();
			return false;
		}
	}
	
	start(e : Event) {
		if (e.target == this.menu) {
			this.eventCoords = getHtmlCoords(e)
			this.offsetCoords = {
				x: number(this.menu.style.left) - this.eventCoords.x,
				y: number(this.menu.style.top) - this.eventCoords.y 
			}; 
			this.moving = this.menu;
		}
	}

	end(e : Event) {
		if (this.moving) {
			const cc = getHtmlCoords(e);
			if ((this.eventCoords.x == cc.x) && (this.eventCoords.y == cc.y)) {
				this.destroy();
				e.stopPropagation();
			}					
		}
	}
	
	add(cb : callback) {
		this.callbacks.push(cb);
	}
	
	/**
	 * Creates the context menu within the main svg element,
	 * positioning it relative to the event that created it.
	 */
	get(event: Event) {
		let theForm = document.querySelector("#contextMenu-form");
		if (theForm) {
			return theForm;
		} else {
			const ctxMenu = document.createElement("div");
			ctxMenu.setAttribute("id", "contextMenu");
			ctxMenu.setAttribute("class", "contextMenu");
			ctxMenu.setAttribute("draggable", "false");
			theForm = form([], 'contextMenu-form');
			ctxMenu.appendChild(theForm);

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
			return theForm;
		}
	}
	
	/**
	 * Call this when the user clicks on an element that might need a context menu.
	 */
	handle(event : Event) {
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

	/** 
	 * Short-hand way of adding a single control to the context menu
	 */
	addControl(event : Event, imageUrl : string, title: string, clickListener: (e: Event) => void, set = "Actions", imageAtts: object) {
		const htmlElement = this.get(event);
		let fs = document.getElementById("#contextMenu-"+set);
		if (!fs) {
			fs = fieldset(set, [], {'id' : "#contextMenu-"+set});
			htmlElement.appendChild(fs);			
		}
		const out = icon('_cm-'+title, title, imageUrl, clickListener, imageAtts)
		fs.appendChild(out);
		
		return out;
	}
	
	/**
	 * Removes all the content from the context menu
	 */
	clear(event: Event) {
		const htmlElement = this.get(event);
		Array.from(htmlElement.children).forEach(e => {
			htmlElement.removeChild(e);
		});
	}

}
