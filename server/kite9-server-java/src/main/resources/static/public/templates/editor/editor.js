/**
 * This composes the basic edit behaviour of the application
 */

// classes

import { Containment } from '/public/classes/containment/containment.js'
import { Property } from '/public/classes/property/property.js'

// Behaviours

// dragable
import { initCompleteDragable, initDragableDragLocator } from '/public/behaviours/dragable/dragable.js' 

// selectable
import { initDeleteContextMenuCallback } from '/public/behaviours/selectable/delete/selectable-delete.js'
import { initReplaceContextMenuCallback } from '/public/behaviours/selectable/replace/selectable-replace.js'
import { initPaletteContextMenuCallback, initMenuPaletteCallback, initPaletteFinder } from '/public/behaviours/palettes/menu/palettes-menu.js'
import { initXCPContextMenuCallback } from '/public/behaviours/selectable/xcp/xcp.js'
import { initSelectable, clearSelectable } from '/public/behaviours/selectable/selectable.js'

// indication
import { initToggleInstrumentationCallback } from '/public/behaviours/indication/toggle/toggle.js'

// defaults
import { initPaletteUpdateDefaults, initSetDefaultContextMenuCallback } from '/public/behaviours/palettes/template/palettes-template.js';

// navigation
import { closeMetadataCallback, initCloseInstrumentationCallback } from "/public/behaviours/navigable/close/close.js";

// undo, redo, revisions
import { initUndoableInstrumentationCallback, initUndoableCommandCallback } from "/public/behaviours/revisioned/undoable.js";

// editing
import { initXMLContextMenuCallback } from '/public/behaviours/editable/xml/editable-xml.js'
import { initEditContextMenuCallback } from '/public/behaviours/editable/text/editable-text.js'
import { initEditableImageContextMenuCallback } from '/public/behaviours/editable/image/editable-image.js'

// style
import { initStyleMenuContextMenuCallback } from '/public/behaviours/styleable/menu/styleable-menu.js'
import { initFillBuildControls, fillSelector, fillIcon, initFillChangeEvent} from '/public/behaviours/styleable/fill/styleable-fill.js'
import { initAlignBuildControls, alignSelector, alignIcon } from '/public/behaviours/styleable/align/styleable-align.js'
import { initStyleContextMenuCallback } from '/public/behaviours/styleable/styleable.js'
import { initFontBuildControls, fontSelector, fontIcon} from '/public/behaviours/styleable/font/styleable-font.js'
import { initTextBuildControls, textSelector, textIcon} from '/public/behaviours/styleable/text/styleable-text.js'

import { once } from '/public/bundles/ensure.js'

/**
 * These are the global variables containing all of the classes used by the editor, and can be extended by other scripts using the 
 * plugin/behaviour system.
 */
import { command, metadata, transition, instrumentation, dragger, contextMenu, palette, paletteContextMenu, overlay } from '/public/templates/adl/adl.js'

const 
	containment = new Containment(),
	layout = new Property("layout"),
	placement = new Property("placement"),
	stylemenu = [];
	
export { command, metadata, transition, instrumentation, dragger, contextMenu, containment, palette, layout, placement, paletteContextMenu, overlay, stylemenu };

function initEditor() {
	
	metadata.add(closeMetadataCallback);
	
	instrumentation.add(initCloseInstrumentationCallback(metadata.isEditor()));
			
	if (metadata.isEditor()) {
		command.add(initUndoableCommandCallback(command));
		
		layout.setCallback(() => command.perform());
		placement.setCallback(() => command.perform());
	
		dragger.dropWith(initCompleteDragable(command));
		dragger.dragLocator(initDragableDragLocator());
	
		
		palette.add(initMenuPaletteCallback(paletteContextMenu));
		palette.addUpdate(() => paletteContextMenu.destroy());
		palette.addUpdate(() => clearSelectable(palette.get()));
		palette.addUpdate(initPaletteUpdateDefaults(palette, initPaletteFinder()));

		
		instrumentation.add(initUndoableInstrumentationCallback(command));
		
		contextMenu.add(initDeleteContextMenuCallback(command));
		contextMenu.add(initEditContextMenuCallback(command));
		contextMenu.add(initXMLContextMenuCallback(command));
		contextMenu.add(initEditableImageContextMenuCallback(command, metadata));
		contextMenu.add(initPaletteContextMenuCallback(palette));
		contextMenu.add(initStyleMenuContextMenuCallback(stylemenu));
		
		stylemenu.push(initStyleContextMenuCallback(command, overlay, fillIcon, 'Fill & Style', initFillBuildControls(), fillSelector, () => "", initFillChangeEvent));
		stylemenu.push(initStyleContextMenuCallback(command, overlay, alignIcon, 'Align', initAlignBuildControls(), alignSelector));
		stylemenu.push(initStyleContextMenuCallback(command, overlay, fontIcon, 'Font', initFontBuildControls(), fontSelector));
		stylemenu.push(initStyleContextMenuCallback(command, overlay, textIcon, 'Text Layout', initTextBuildControls(), textSelector));
		
		paletteContextMenu.add(initReplaceContextMenuCallback(palette, command, {keptAttributes: ['id', 'reference', 'end', 'drawDirection'], keptTags: ['from', 'to' ]}, containment));
		//contextMenu.add(initXCPContextMenuCallback(command, metadata, containment));
		
		initSelectable(() => palette.get().querySelectorAll("[k9-elem][id]"), palette.get(), true);

	}
	
	instrumentation.add(initToggleInstrumentationCallback());
	initSelectable();  // for main svg agrea

}

once(() => initEditor());
