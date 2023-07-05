/**
 * This composes the basic edit behaviour of the application
 */

// classes

import { Containment } from '../../classes/containment/containment.js'
import { Property } from '../../classes/context-menu/property.js'

// Behaviours

// dragable
import { initCompleteDragable, initDragableDragLocator, initDragContextMenuCallback } from '../../behaviours/dragable/dragable.js' 

// selectable
import { initDeleteContextMenuCallback } from '../../behaviours/selectable/delete/selectable-delete.js'
import { initReplaceContextMenuCallback } from '../../behaviours/selectable/replace/selectable-replace.js'
import { initPaletteContextMenuCallback, initMenuPaletteCallback, initPaletteFinder } from '../../behaviours/palettes/menu/palettes-menu.js'
import { initSelectable, clearLastSelected } from '../../behaviours/selectable/selectable.js'

// indication
import { initToggleInstrumentationCallback } from '../../behaviours/indication/toggle/toggle.js'

// defaults
import { initPaletteUpdateDefaults } from '../../behaviours/palettes/template/palettes-template.js';

// navigation
import { closeMetadataCallback, initCloseInstrumentationCallback } from "../../behaviours/navigable/close/navigable-close.js";

// undo, redo, revisions
import { initUndoableInstrumentationCallback, initUndoableCommandCallback } from "../../behaviours/revisioned/undoable.js";

// editing
import { initXMLContextMenuCallback } from '../../behaviours/editable/xml/editable-xml.js'
import { initEditContextMenuCallback } from '../../behaviours/editable/text/editable-text.js'
import { initEditableImageContextMenuCallback } from '../../behaviours/editable/image/editable-image.js'

// style
import { initStyleMenuContextMenuCallback } from '../../behaviours/styleable/menu/styleable-menu.js'
import { initFillBuildControls, fillSelector, fillIcon, initFillChangeEvent} from '../../behaviours/styleable/fill/styleable-fill.js'
import { initAlignBuildControls, alignSelector, alignIcon } from '../../behaviours/styleable/align/styleable-align.js'
import { initStyleContextMenuCallback } from '../../behaviours/styleable/styleable.js'
import { initFontBuildControls, fontSelector, fontIcon} from '../../behaviours/styleable/font/styleable-font.js'
import { initTextBuildControls, textSelector, textIcon} from '../../behaviours/styleable/text/styleable-text.js'

// containment rules (by type)
import { initTypedRulesContainsCallback, initTypedRulesTypeCallback, initTypedRulesContainmentRuleCallback} from '../../behaviours/typed/rules/typed-rules.js'

import { once } from '../../bundles/ensure.js'

/**
 * These are the global variables containing all of the classes used by the editor, and can be extended by other scripts using the 
 * plugin/behaviour system.
 */
import { command, metadata, transition, instrumentation, dragger, contextMenu, palette, paletteContextMenu, overlay } from '../../templates/adl/adl.js'
import { initOptionalDirectionContextMenuCallback } from '../../behaviours/direction/direction.js'

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
	
		
		palette.addLoad(initMenuPaletteCallback(paletteContextMenu));
		palette.addReveal(() => paletteContextMenu.destroy());
		palette.addReveal(() => clearLastSelected(palette.get()));
		palette.addReveal(initPaletteUpdateDefaults(palette, initPaletteFinder()));
		
		instrumentation.add(initUndoableInstrumentationCallback(command));
		
		contextMenu.add(initDragContextMenuCallback(dragger));
		contextMenu.add(initPaletteContextMenuCallback(palette));
		contextMenu.add(initStyleMenuContextMenuCallback(stylemenu));
		contextMenu.add(initDeleteContextMenuCallback(command));
		contextMenu.add(initEditContextMenuCallback(command));
		contextMenu.add(initXMLContextMenuCallback(command));
		contextMenu.add(initEditableImageContextMenuCallback(command, metadata));
		contextMenu.add(initOptionalDirectionContextMenuCallback(command));
		
		stylemenu.push(initStyleContextMenuCallback(command, overlay, fillIcon, 'Fill & Style', initFillBuildControls(), fillSelector, () => "", initFillChangeEvent));
		stylemenu.push(initStyleContextMenuCallback(command, overlay, alignIcon, 'Align', initAlignBuildControls(), alignSelector));
		stylemenu.push(initStyleContextMenuCallback(command, overlay, fontIcon, 'Font', initFontBuildControls(), fontSelector));
		stylemenu.push(initStyleContextMenuCallback(command, overlay, textIcon, 'Text Layout', initTextBuildControls(), textSelector));
		
		paletteContextMenu.add(initReplaceContextMenuCallback(palette, command, {keptAttributes: ['id', 'reference', 'end', 'style']}, containment));
		//contextMenu.add(initXCPContextMenuCallback(command, metadata, containment));
		
		initSelectable(palette.get(), true);
		
		containment.addTypeCallback(initTypedRulesTypeCallback());
		containment.addContainsCallback(initTypedRulesContainsCallback());
		containment.addContainmentRuleCallback(initTypedRulesContainmentRuleCallback());

	}
	
	instrumentation.add(initToggleInstrumentationCallback());
	initSelectable();  // for main svg area

}
 
once(() => initEditor());
