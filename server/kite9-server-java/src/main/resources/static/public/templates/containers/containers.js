
import { once } from '/public/bundles/ensure.js'

import { command, metadata, dragger, contextMenu, layout, containment, palette, paletteContextMenu, overlay, stylemenu } from '/public/templates/editor/editor.js'

//Containers
import { initInsertContextMenuCallback, initInsertDragLocator } from '/public/behaviours/containers/insert/containers-insert.js'
import { initContainContextMenuCallback } from '/public/behaviours/containers/contain/containers-contain.js'
import { initContainerLayoutMoveCallback, initLayoutContextMenuCallback, initContainerLayoutPropertyFormCallback, initContainerLayoutPropertySetCallback } from '/public/behaviours/containers/layout/containers-layout.js'
import { initChildContextMenuCallback } from '/public/behaviours/containers/child/containers-child.js'
import { initContainerDropLocatorFunction, initContainerDropCallback } from '/public/behaviours/containers/drag/containers-drag.js' 
import { initAttributeContainmentCallback } from '/public/behaviours/containers/rules/containers-rules.js'
import { initMarginsBuildControls, marginsIcon,  initPaddingBuildControls, paddingIcon,   initMinSizeBuildControls, minSizeIcon, sizingEnumProperties, sizingEnumValues, containerSizingSelector, sizingIcon } from '/public/behaviours/styleable/size/styleable-size.js'
import { initStyleContextMenuCallback, initBasicBuildControls } from '/public/behaviours/styleable/styleable.js'
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

		const margins = initStyleContextMenuCallback(command, overlay,  
			marginsIcon,
			'Margins',
			initMarginsBuildControls(),
			containerSizingSelector
			);

		const padding = initStyleContextMenuCallback(command, overlay,  
			paddingIcon,
			'Padding',
			initPaddingBuildControls(),
			containerSizingSelector
			);

		const minSize = initStyleContextMenuCallback(command, overlay,  
			minSizeIcon,
			'Minimum Size',
			initMinSizeBuildControls(),
			containerSizingSelector
			);

		const sizing = initStyleContextMenuCallback(command, overlay,  
			sizingIcon,
			'Sizing Rules',
			initBasicBuildControls(sizingEnumProperties, sizingEnumValues),
			containerSizingSelector
			);
			
		const traversal = initStyleContextMenuCallback(command, overlay, 
			traversalIcon,
			'Link Traversal Rules',
			initBasicBuildControls(traversalEnumProperties, traversalEnumValues),
			containerSizingSelector,
			(r) => '');
		
		stylemenu.push(margins);
		stylemenu.push(padding);
		stylemenu.push(minSize);
		stylemenu.push(sizing);
		stylemenu.push(traversal);
	}
	
}


once(() => initContainers());