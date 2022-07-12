import { getHtmlCoords } from '/github/kite9-org/kite9/client/bundles/screen.js?v=v0.8'


/**
 * This allows us to draw extra controls around the edge of a diagram element, which you can click or drag.
 */
export class Overlay {
	
	constructor(cb) {
		this.callbacks = cb == undefined ? [] : cb;

		var cssId = 'overlay';  
		if (!document.getElementById(cssId)) {
		    var head  = document.getElementsByTagName('head')[0];
		    var link  = document.createElement('link');
		    link.id   = cssId;
		    link.rel  = 'stylesheet';
		    link.type = 'text/css';
		    link.href = '/github/kite9-org/kite9/client/classes/overlay/overlay.css?v=v0.8';
		    link.media = 'all';
		    head.appendChild(link);
		}
		
		this.overlays = [];
		this.targetElement = null;
	}
	
	/**
	 * Removes all the overlays from the screen.
	 */
	destroy() {
		this.overlays.forEach(o => o.parentElement.removeChild(o));
		this.overlays = [];
	}

	addOverlay(element, imageUrl, title, addListener, x, y, direction) {
		if (element != this.targetElement) {
			// remove old overlays
			destroy();
			this.targetElement = element;
		}
		
		var img = document.createElement("img");
		htmlElement.appendChild(img);
		img.setAttribute("title", title);
		img.setAttribute("src", imageUrl);
		img.setAttribute("class", "overlay "+direction);
		img.setAttribute("top", y);
		img.setAttribute("left", x);
		addListener(img);
	}

}
