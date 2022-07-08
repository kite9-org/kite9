import { ensureCss } from '/github/kite9-org/kite9/client/bundles/ensure.js?v=v0.5'

export class Modal {

	constructor(id, cb) {
		this.id = id;
		this.callbacks = cb == undefined ? [] : cb;
		this.expanded = {};

		ensureCss('/public/client/classes/modal/modal.css?v=v0.5');
		
		var darken = document.getElementById("_darken");
		if (!darken) {
			darken = document.createElement("div");
			darken.setAttribute("id", "_darken");
			darken.setAttribute("class", "darken");
			document.querySelector("body").appendChild(darken);
			darken.style.display = 'none';
		}
		
		var modal = document.getElementById(this.id);
		if (!modal) {
			// create modal
			modal = document.createElement("div");
			modal.setAttribute("id", this.id);
			modal.setAttribute("class", "modal");
			document.querySelector("body").appendChild(modal);
						
			// create content area
			var content = document.createElement("div");
			content.setAttribute("class", "content");
			modal.appendChild(content);
		}
	}
	
	getId() {
		return this.id;
	}
	
	get(event) {
	}
	
	getContent(event) {
		return document.getElementById(this.id).querySelector("div.content");		
	}
	
	getOpenEvent() {
		return this.openEvent;
	}
	
	/**
	 * Removes all the content
	 */
	clear(event) {
		var content = this.getContent(event);
		Array.from(content.children).forEach(e => {
			content.removeChild(e);
		});
	}
		
	open(event) {
		this.openEvent = event;
		
		var darken = document.getElementById("_darken");
		var modal = document.getElementById(this.id);
								
		modal.style.visibility = 'visible';
		darken.style.display = 'block';
		
		return modal;			
	}
	
	destroy() {
		var modal = document.getElementById(this.id);
		var darken = document.getElementById("_darken");
		modal.style.visibility = 'hidden';
		darken.style.display = 'none';
	}
}