
import { once } from '/public/bundles/ensure.js'

import { command, metadata, dragger, contextMenu, layout, containment, palette, paletteContextMenu } from '/public/templates/editor/editor.js'

//Containers
import { initInsertContextMenuCallback, initInsertDragLocator } from '/public/behaviours/containers/insert/containers-insert.js'
import { initContainContextMenuCallback } from '/public/behaviours/containers/contain/containers-contain.js'
import { initContainerLayoutMoveCallback, initLayoutContextMenuCallback, initContainerLayoutPropertyFormCallback, initContainerLayoutPropertySetCallback } from '/public/behaviours/containers/layout/containers-layout.js'
import { initChildContextMenuCallback } from '/public/behaviours/containers/child/containers-child.js'
import { initContainerDropLocatorFunction, initContainerDropCallback } from '/public/behaviours/containers/drag/containers-drag.js' 
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
				
		contextMenu.add(initLayoutContextMenuCallback(layout));
		contextMenu.add(initChildContextMenuCallback(command));
		
		paletteContextMenu.add(initContainContextMenuCallback(palette, command, containment)); 
		paletteContextMenu.add(initInsertContextMenuCallback(palette, command, containment));

	}
	
}


once(() => initContainers());