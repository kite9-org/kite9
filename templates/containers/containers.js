
import { once } from '/public/bundles/ensure.js'

import { command, metadata, dragger, contextMenu, layout, containment, palette } from '/github/kite9-org/kite9/templates/editor/editor.js?v=v0.9'

//Containers
import { initInsertPaletteCallback, initInsertContextMenuCallback, initInsertDragLocator } from '/public/behaviours/containers/insert/containers-insert.js'
import { initContainPaletteCallback, initContainContextMenuCallback } from '/public/behaviours/containers/contain/containers-contain.js'
import { initContainerLayoutMoveCallback, initLayoutContextMenuCallback, initContainerLayoutPropertyFormCallback, initContainerLayoutPropertySetCallback } from '/public/behaviours/containers/layout/containers-layout.js'
import { initChildContextMenuCallback } from '/public/behaviours/containers/child/containers-child.js'
import { initContainerDropLocatorFunction, initContainerDropCallback } from '/github/kite9-org/kite9/client/behaviours/containers/drag/containers-drag.js' 
import { initAttributeContainmentCallback } from '/public/behaviours/containers/rules/containers-rules.js'


function initContainers() {
	
	containment.add(initAttributeContainmentCallback());

	if (metadata.isEditor()) {

		layout.formCallback(initContainerLayoutPropertyFormCallback()); 
		layout.setCallback(initContainerLayoutPropertySetCallback(command)); 

		dragger.dragLocator(initInsertDragLocator());
		dragger.moveWith(initContainerLayoutMoveCallback());
		dragger.dropWith(initContainerDropCallback(command, containment));
		dragger.dropLocatorFn(initContainerDropLocatorFunction(containment));

		palette.add(initContainPaletteCallback(command));
		palette.add(initInsertPaletteCallback(command, containment));

		
		contextMenu.add(initContainContextMenuCallback(palette, containment)); 
		contextMenu.add(initInsertContextMenuCallback(palette, containment));
		contextMenu.add(initLayoutContextMenuCallback(layout));
		contextMenu.add(initChildContextMenuCallback(command));

	}
	
}


once(() => initContainers());