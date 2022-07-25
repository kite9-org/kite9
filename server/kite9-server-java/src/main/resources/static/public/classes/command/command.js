import { getMainSvg } from "/public/bundles/screen.js";
import { encodeADLElement } from '/public/bundles/api.js'

/**
 * Command handles the flow of commands through the system, and the undo/redo log and
 * keeps track of the ADL for the current document.
 */
export class Command {
	
	constructor() {
		this.commandList = [];
		this.history = [];
		this.callbacks = [];
		this.version = 0;
		this.base64adl = getMainSvg().getElementById("adl:markup").textContent;
		this.doc = undefined;
	}
	
	add(cb) {
		// NB: additions go at the start of the callback structure.
		this.callbacks.unshift(cb);
	}
	
	adlUpdated(base64adl) {
		this.base64adl = base64adl;
		this.doc = undefined;
	}
	
	getAdl(id) {
		const el = this.getADLDom(id);
		if (el == undefined) {
			return undefined;
		}
		const elText = new XMLSerializer().serializeToString(el);
		return encodeADLElement(elText);
	}
	
	getADLDom(id) {
		if (this.doc == undefined) {
			var text = atob(this.base64adl);
			var parser = new DOMParser();
			this.doc = parser.parseFromString(text, "application/xml");
		}
		
		return this.doc.getElementById(id);
	}
	
	canUndo() {
		return this.version > 0;
	}
	
	canRedo() {
		return this.history.length > this.version;
	}
	
	undo() {
		if (this.version > 0) {
			this.version --;
			const toUndo = this.history[this.version];
			const update = {
				type: 'UNDO',
				base64adl: this.base64adl,
				commands: toUndo.commands
			}
			this.callbacks.forEach(cb => cb(update));
		}
	}
	
	
	redo() {
		if (this.version < this.history.length) {
			const toUndo = this.history[this.version];
			this.version ++;
			const update = {
				type: 'REDO',
				base64adl: this.base64adl,
				commands: toUndo.commands
			}
			this.callbacks.forEach(cb => cb(update));
		}
	}
	
	push(command) {
		this.commandList.push(command);
	}
	
	pushAllAndPerform(commands) {
		this.commandList = this.commandList.concat(commands);
		this.perform();
	}
	
	perform() {
		const update = {
			type: 'NEW',
			base64adl: this.base64adl,
			commands: this.commandList	
		};
		
		// tracking for undo / redo log
		this.history = this.history.slice(0, this.version);
		this.history.push(update);
		this.version ++;
		this.commandList = [];
		
		this.callbacks.forEach(cb => cb(update));
	}
	
}

/**
 * If the SVG document is returned containing the base64-encoded ADL
 * markup, make sure to update it here.
 */
export function initCommandTransitionCallback(command) {
	
	return function(doc) {
		const adl = doc.getElementById("adl:markup");
		if (adl) {
			command.adlUpdated(adl.textContent);
		}
	}
	
} 
	