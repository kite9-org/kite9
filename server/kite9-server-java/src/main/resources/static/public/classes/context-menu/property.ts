import { ContextMenu } from '../context-menu/context-menu.js'

type FormCallback = (p: Property,
	e: Event,
	c: ContextMenu,
	s: Element[]) => void
	
type SetCallback = (p: Property,
	contextEvent: Event,
	formEvent: Event,
	c: ContextMenu,
	s: Element[]) => void

/**
 * Handles property functions where there might need to be pluggable behaviour.
 */
export class Property {

	name : string;
	formCallbacks : FormCallback[] = [];
	setCallbacks : SetCallback[] = [];
	
	constructor(name : string) {
		this.name = name;
	}
	
	formCallback(cb : FormCallback) {
		this.formCallbacks.push(cb);
	}
	
	setCallback(cb: SetCallback) {
		// nb: additions are added to front of array
		this.setCallbacks.unshift(cb);
	}
	
	/**
	 * Adds controls to the contextMenu for editing the property.
	 */
	populateForm(contextEvent: Event, contextMenu: ContextMenu, selectedElements: Element[]) {
		this.formCallbacks.forEach(fc => fc(this, contextEvent, contextMenu, selectedElements));
	}
	
	/**
	 * Use this as the callback for "ok" buttons on the property to ensure all callbacks 
	 * are done.
	 */
	setProperty(contextEvent: Event, formEvent: Event, contextMenu: ContextMenu, selectedElements: Element[]) {
		this.setCallbacks.forEach(fc => fc(this, contextEvent, formEvent, contextMenu, selectedElements));
		contextMenu.destroy();
	}
}

