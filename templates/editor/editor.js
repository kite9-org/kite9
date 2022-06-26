/**
 * This composes the basic edit behaviour of the application
 */

// classes

import { Containment } from '/github/kite9-org/kite9/classes/containment/containment.js';
import { Property } from '/github/kite9-org/kite9/classes/property/property.js';

// Behaviours

// dragable
import { initCompleteDragable, initDragableDragLocator } from '/github/kite9-org/kite9/behaviours/dragable/dragable.js' 

// selectable
import { initDeleteContextMenuCallback } from '/github/kite9-org/kite9/behaviours/selectable/delete/selectable-delete.js';
import { initReplaceContextMenuCallback, initReplacePaletteCallback } from '/github/kite9-org/kite9/behaviours/selectable/replace/selectable-replace.js';
import { initXCPContextMenuCallback } from '/github/kite9-org/kite9/behaviours/selectable/xcp/xcp.js';
import { initSelectable } from '/github/kite9-org/kite9/behaviours/selectable/selectable.js';

// indication
import { initToggleInstrumentationCallback } from '/github/kite9-org/kite9/behaviours/indication/toggle/toggle.js';

// navigation
import { closeMetadataCallback, initCloseInstrumentationCallback } from "/github/kite9-org/kite9/behaviours/navigable/close/close.js";

// undo, redo, revisions
import { initUndoableInstrumentationCallback, initUndoableCommandCallback } from "/github/kite9-org/kite9/behaviours/revisioned/undoable.js";

// editing
import { initXMLContextMenuCallback } from '/github/kite9-org/kite9/behaviours/editable/xml/editable-xml.js';
import { initEditContextMenuCallback } from '/github/kite9-org/kite9/behaviours/editable/text/editable-text.js';
import { initEditableImageContextMenuCallback } from '/github/kite9-org/kite9/behaviours/editable/image/editable-image.js';

import { once } from '/github/kite9-org/kite9/bundles/ensure.js';

/**
 * These are the global variables containing all of the classes used by the editor, and can be extended by other scripts using the 
 * plugin/behaviour system.
 */
import { command, metadata, transition, instrumentation, dragger, contextMenu, palette } from '/github/kite9-org/kite9/templates/adl/adl.js';

const 
	containment = new Containment(),
	layout = new Property("layout");
	
export { command, metadata, transition, instrumentation, dragger, contextMenu, containment, palette, layout };

function initEditor() {
	
	metadata.add(closeMetadataCallback);
	
	instrumentation.add(initCloseInstrumentationCallback(metadata.isEditor()));
			
	if (metadata.isEditor()) {
		command.add(initUndoableCommandCallback(command));
		layout.setCallback(() => command.perform());
	
		dragger.dropWith(initCompleteDragable(command));
		dragger.dragLocator(initDragableDragLocator());
	
		
		palette.add(initReplacePaletteCallback(command, {keptAttributes: ['id', 'reference', 'end', 'drawDirection', 'style'], keptTags: ['from', 'to' ]}, containment));
		
		instrumentation.add(initUndoableInstrumentationCallback(command));
		
		contextMenu.add(initDeleteContextMenuCallback(command));
		contextMenu.add(initReplaceContextMenuCallback(palette, containment)); 
		contextMenu.add(initEditContextMenuCallback(command));
		contextMenu.add(initXCPContextMenuCallback(command, metadata, containment));
		contextMenu.add(initXMLContextMenuCallback(command));
		contextMenu.add(initEditableImageContextMenuCallback(command, metadata));
	}
	
	instrumentation.add(initToggleInstrumentationCallback());
	initSelectable();

}

once(() => initEditor());
