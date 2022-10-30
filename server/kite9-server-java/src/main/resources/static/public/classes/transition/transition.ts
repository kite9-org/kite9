import { Timeline } from './timeline.js'

export type TransitionDocumentCallback = (d: Document) => void
export type TransitionAnimationCallback = (e: Element, tl: Timeline) => void

/*
 * This class handles the loading of new versions of the svg document 
 * and orchestrates animation between the versions.
 * Has callbacks for when a new version arrives, and for animating it.
 */
export class Transition {

	animationCallbacks: TransitionAnimationCallback[] = [];
	documentCallbacks: TransitionDocumentCallback[] = [];

	document(cb: TransitionDocumentCallback) {
		this.documentCallbacks.push(cb);
	}

	animation(cb: TransitionAnimationCallback) {
		this.animationCallbacks.push(cb);
	}


	change(doc: Document) {
		// call the document callbacks
		this.documentCallbacks.forEach(cb => cb(doc));

		// create the animation timeline
		const tl = new Timeline(1000);

		this.animationCallbacks.forEach(cb => cb(doc.documentElement, tl))
		tl.play();

		// force the DOMContentLoaded event to occur again
		const evt = document.createEvent('Event');
		evt.initEvent('DOMContentLoaded', false, false);
		window.dispatchEvent(evt);
	}

}