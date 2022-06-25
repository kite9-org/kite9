import { ensureCss } from '/public/bundles/ensure.js';

/**
 * Provides functionality for populating the instrumentation menu, keyboard shortcuts and interaction state (keys/mouse/touch etc).
 */
export class Instrumentation {
	
	constructor() {
		this.callbacks = [];

		ensureCss('/public/classes/instrumentation/instrumentation.css');
		
		this.nav = document.getElementById("_instrumentation");
		if (this.nav == undefined) {
			this.nav = document.createElement("div");
			this.nav.setAttribute("id", "_instrumentation");
			this.nav.setAttribute("class", "instrumentation");
			document.querySelector("body").appendChild(this.nav);
		}

	}
	
	add(cb) {
		this.callbacks.push(cb);
		setTimeout(() => cb(this.nav), 0);
	}
	
	update() {
		this.callbacks.forEach(cb => cb(this.nav));
	}

}

