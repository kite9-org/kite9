import { icon } from '../../bundles/form.js'
import { Command, CommandCallback } from '../../classes/command/command.js';
import { InstrumentationCallback } from '../../classes/instrumentation/instrumentation.js';


let undo: HTMLElement, redo: HTMLElement; 	// buttons

function updateOpacity(command: Command) {
	if (undo) {
		undo.style.opacity = command.canUndo() ? "1" : ".5";
	}

	if (redo) {
		redo.style.opacity = command.canRedo() ? "1" : ".5";
	}
}

function ensureButton(nav: HTMLElement, name: string, cb: (e: Event) => void) : HTMLElement {
	let b = nav.querySelector("#_" + name) as HTMLElement | undefined;

	if (b == undefined) {
		b = icon('_' + name, name,
			"/public/behaviours/revisioned/" + name + ".svg",
			cb);
		nav.appendChild(b);
	}

	return b;
}

/**
 * Provide the on-click actions that will be called when the user hits undo/redo
 */
export function initUndoableInstrumentationCallback(
	command: Command) : InstrumentationCallback {
	return function(nav: HTMLElement) {
		undo = ensureButton(nav, "undo", () => { if (command.canUndo()) { command.undo(); } });
		redo = ensureButton(nav, "redo", () => { if (command.canRedo()) { command.redo(); } });
		updateOpacity(command);
	}
}

export function initUndoableCommandCallback(
	command: Command) : CommandCallback {
	return function() {
		updateOpacity(command);
	}
}