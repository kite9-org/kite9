import { ensureCss } from '/github/kite9-org/kite9/client/bundles/ensure.js?v=v0.4'

/**
 * Provides functionality for populating the instrumentation menu, keyboard shortcuts and interaction state (keys/mouse/touch etc).
 */
export class Instrumentation {
	
	constructor() {
		this.callbacks = [];

		ensureCss('/public/client/classes/instrumentation/instrumentation.css?v=v0.4');
		
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

