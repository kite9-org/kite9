import { Timeline } from '/public/classes/timeline/timeline.js'

/*
 * This class handles the loading of new versions of the svg document 
 * and orchestrates animation between the versions.
 * Has callbacks for when a new version arrives, and for animating it.
 */
export class Transition {
	
	constructor() {
		this.animationCallbacks = [];
		this.documentCallbacks = [];
	}
	
	document(cb) {
		this.documentCallbacks.push(cb);
	}
	
	animation(cb) {
		this.animationCallbacks.push(cb);
	}
	
	
	change(doc) {
		// call the document callbacks
		this.documentCallbacks.forEach(cb => cb(doc));
		
		// create the animation timeline
		var tl = new Timeline(1000);

    	this.animationCallbacks.forEach(cb => cb(doc.documentElement, tl))
		tl.play();
		
		// force the DOMContentLoaded event to occur again
		var evt = document.createEvent('Event');
		evt.initEvent('DOMContentLoaded', false, false);
		window.dispatchEvent(evt);
	}
	
}