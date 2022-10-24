import { ensureCss } from '../../bundles/ensure.js'

type callback = (n: Element) => void

/**
 * Provides functionality for populating the instrumentation menu, keyboard shortcuts and interaction state (keys/mouse/touch etc).
 */
export class Instrumentation {
	
	callbacks : callback[] = [];
	nav : Element | null = undefined
	
	constructor() {

		ensureCss('/public/classes/instrumentation/instrumentation.css');
		
		this.nav = document.getElementById("_instrumentation");
		if (this.nav == undefined) {
			this.nav = document.createElement("div");
			this.nav.setAttribute("id", "_instrumentation");
			this.nav.setAttribute("class", "instrumentation");
			document.querySelector("body").appendChild(this.nav);
		}

	}
	
	add(cb : callback) {
		this.callbacks.push(cb);
		setTimeout(() => cb(this.nav), 0);
	}
	
	update() {
		this.callbacks.forEach(cb => cb(this.nav));
	}

}

