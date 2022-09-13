
import { once } from '/public/bundles/ensure.js'

import { command, metadata, dragger, contextMenu, layout, containment, palette, paletteContextMenu, overlay, stylemenu } from '/public/templates/editor/editor.js'

//Containers
import { initInsertContextMenuCallback, initInsertDragLocator } from '/public/behaviours/containers/insert/containers-insert.js'
import { initContainContextMenuCallback } from '/public/behaviours/containers/contain/containers-contain.js'
import { initContainerLayoutMoveCallback, initLayoutContextMenuCallback, initContainerLayoutPropertyFormCallback, initContainerLayoutPropertySetCallback } from '/public/behaviours/containers/layout/containers-layout.js'
import { initChildContextMenuCallback } from '/public/behaviours/containers/child/containers-child.js'
import { initContainerDropLocatorFunction, initContainerDropCallback } from '/public/behaviours/containers/drag/containers-drag.js' 
import { initAttributeContainmentCallback } from '/public/behaviours/containers/rules/containers-rules.js'
import { initMarginContextMenuCallback, initPaddingContextMenuCallback, initMinimumSizeContextMenuCallback, sizingEnumProperties, sizingEnumValues, containerSizingSelector, sizingIcon } from '/public/behaviours/styleable/size/styleable-size.js'
import { initEnumContextMenuCallback, initBasicBuildControls } from '/public/behaviours/styleable/enum/styleable-enum.js'
import { traversalEnumProperties, traversalEnumValues, traversalIcon } from '/public/behaviours/containers/traversal/containers-traversal.js';

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

		stylemenu.push(initMarginContextMenuCallback(command, overlay));
		stylemenu.push(initPaddingContextMenuCallback(command, overlay));
		stylemenu.push(initMinimumSizeContextMenuCallback(command, overlay));

		const sizing = initEnumContextMenuCallback(command, overlay,  
			sizingIcon,
			'Sizing Rules',
			initBasicBuildControls(sizingEnumProperties, sizingEnumValues),
			containerSizingSelector
			);
			
		const traversal = initEnumContextMenuCallback(command, overlay, 
			traversalIcon,
			'Link Traversal Rules',
			initBasicBuildControls(traversalEnumProperties, traversalEnumValues),
			containerSizingSelector);
		
		stylemenu.push(sizing);
		stylemenu.push(traversal);
	}
	
}


once(() => initContainers());