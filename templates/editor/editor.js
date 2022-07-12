/**
 * This composes the basic edit behaviour of the application
 */

// classes

import { Containment } from '/github/kite9-org/kite9/client/classes/containment/containment.js?v=v0.8'
import { Property } from '/github/kite9-org/kite9/client/classes/property/property.js?v=v0.8'

// Behaviours

// dragable
import { initCompleteDragable, initDragableDragLocator } from '/github/kite9-org/kite9/client/behaviours/dragable/dragable.js?v=v0.8' 

// selectable
import { initDeleteContextMenuCallback } from '/github/kite9-org/kite9/client/behaviours/selectable/delete/selectable-delete.js?v=v0.8'
import { initReplaceContextMenuCallback, initReplacePaletteCallback } from '/github/kite9-org/kite9/client/behaviours/selectable/replace/selectable-replace.js?v=v0.8'
import { initXCPContextMenuCallback } from '/github/kite9-org/kite9/client/behaviours/selectable/xcp/xcp.js?v=v0.8'
import { initSelectable } from '/github/kite9-org/kite9/client/behaviours/selectable/selectable.js?v=v0.8'

// indication
import { initToggleInstrumentationCallback } from '/github/kite9-org/kite9/client/behaviours/indication/toggle/toggle.js?v=v0.8'

// navigation
import { closeMetadataCallback, initCloseInstrumentationCallback } from "/github/kite9-org/kite9/client/behaviours/navigable/close/close.js?v=v0.8";

// undo, redo, revisions
import { initUndoableInstrumentationCallback, initUndoableCommandCallback } from "/github/kite9-org/kite9/client/behaviours/revisioned/undoable.js?v=v0.8";

// editing
import { initXMLContextMenuCallback } from '/github/kite9-org/kite9/client/behaviours/editable/xml/editable-xml.js?v=v0.8'
import { initEditContextMenuCallback } from '/github/kite9-org/kite9/client/behaviours/editable/text/editable-text.js?v=v0.8'
import { initEditableImageContextMenuCallback } from '/github/kite9-org/kite9/client/behaviours/editable/image/editable-image.js?v=v0.8'

import { once } from '/github/kite9-org/kite9/client/bundles/ensure.js?v=v0.8'

/**
 * These are the global variables containing all of the classes used by the editor, and can be extended by other scripts using the 
 * plugin/behaviour system.
 */
import { command, metadata, transition, instrumentation, dragger, contextMenu, palette } from '/github/kite9-org/kite9/templates/adl/adl.js?v=v0.8'

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
