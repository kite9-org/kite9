import { getMainSvg } from "../../bundles/screen.js";
import { encodeADLElement } from '../../bundles/api.js'
import { TransitionDocumentCallback } from "../transition/transition.js";

export type SingleCommand = {
	type?: string,
	fragmentId? : string,
	from?: string,
	fromBefore?: string,
	to?: string,
	name?: string,
	horiz?: string,
	uriStr?: string,
	newId?: string,
	xpathToValue?: object,
	base64Element?: string,
	moveId?: string,
	containedIds?: string[],
	beforeId?: string,
	keptAttributes?: string[],
	keptTags?: string[]
}

export type Update = {
	type: string,
	base64adl: string,
	commands: SingleCommand[]	
}

export type CommandCallback = (u: Update) => void

/**
 * Command handles the flow of commands through the system, and the undo/redo log and
 * keeps track of the ADL for the current document.
 */
export class Command {

	commandList : SingleCommand[] = []
	callbacks  : CommandCallback[] = []
	history : Update[] = []
	version = 0
	base64adl = ''
	doc : Document | null = null
	
	constructor() {
		this.base64adl = getMainSvg().getElementById("adl:markup").textContent;
		this.doc = undefined;
	}
	
	add(cb : CommandCallback) {
		// NB: additions go at the start of the callback structure.
		this.callbacks.unshift(cb);
	}
	
	adlUpdated(base64adl : string) {
		this.base64adl = base64adl;
		this.doc = undefined;
	}
	
	getAdl(id : string) {
		const el = this.getADLDom(id);
		if (el == undefined) {
			return undefined;
		}
		const elText = new XMLSerializer().serializeToString(el);
		return encodeADLElement(elText);
	}
	
	getADLDom(id : string) {
		if (this.doc == undefined) {
			const text = atob(this.base64adl);
			const parser = new DOMParser();
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
	
	push(command : SingleCommand) {
		this.commandList.push(command);
	}
	
	pushAllAndPerform(commands : SingleCommand[]) {
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
export function initCommandTransitionCallback(command : Command) : TransitionDocumentCallback {
	
	return function(doc : Document) {
		const adl = doc.getElementById("adl:markup");
		if (adl) {
			command.adlUpdated(adl.textContent);
		}
	}
	
} 
	