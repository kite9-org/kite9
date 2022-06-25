import { icon } from '/public/bundles/form.js';


var undo, redo; 	// buttons

function updateOpacity(command) {
	if (undo) {
		undo.style.opacity = command.canUndo() ? 1 : .5;
	}
	
	if (redo) {
		redo.style.opacity = command.canRedo() ? 1 : .5;
	}
}

function ensureButton(nav, name, cb) {
	var b = nav.querySelector("#_"+name);
	
	if (b == undefined) {
		var b = icon('_'+name, name, 
				 "/public/behaviours/revisioned/"+name+".svg",
				 cb);
		nav.appendChild(b);
	}
	
	return b;
}

/**
 * Provide the on-click actions that will be called when the user hits undo/redo
 */
export function initUndoableInstrumentationCallback(command) {
	return function(nav) {
		undo = ensureButton(nav, "undo", (evt) => { if (command.canUndo()) { command.undo(); }} );
		redo = ensureButton(nav, "redo", (evt) => { if (command.canRedo()) { command.redo(); }} );
		updateOpacity(command);
	}
}

export function initUndoableCommandCallback(command) {
	return function() {
		updateOpacity(command);
	}
}