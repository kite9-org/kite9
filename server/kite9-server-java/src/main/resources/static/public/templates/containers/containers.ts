
import { once } from '../../bundles/ensure.js'

import { command, metadata, dragger, contextMenu, layout, containment, palette, paletteContextMenu, overlay, stylemenu } from '../../templates/editor/editor.js'

//Containers
import { initInsertContextMenuCallback } from '../../behaviours/containers/insert/containers-insert.js'
import { initContainContextMenuCallback } from '../../behaviours/containers/contain/containers-contain.js'
import { initContainerLayoutMoveCallback, initLayoutContextMenuCallback, initContainerLayoutPropertyFormCallback, initContainerLayoutPropertySetCallback, initLayoutIndicator, initLayoutIndicatorPaletteRevealCallback } from '../../behaviours/containers/layout/containers-layout.js'
import { initChildContextMenuCallback } from '../../behaviours/containers/child/containers-child.js'
import { initContainerDropLocatorCallback, initContainmentDropCallback } from '../../behaviours/containers/drag/containers-drag.js' 
import { initAttributeContainmentCallback } from '../../behaviours/containers/rules/containers-rules.js'
import { initMarginsBuildControls, marginsIcon,  initPaddingBuildControls, paddingIcon,   initMinSizeBuildControls, minSizeIcon, sizingEnumProperties, sizingEnumValues, containerSizingSelector, sizingIcon } from '../../behaviours/styleable/size/styleable-size.js'
import { initStyleContextMenuCallback, initBasicBuildControls } from '../../behaviours/styleable/styleable.js'
import { traversalEnumProperties, traversalEnumValues, traversalIcon } from '../../behaviours/containers/traversal/containers-traversal.js';
import { isRectangular } from '../../bundles/api.js'

function initContainers() {
	
	containment.add(initAttributeContainmentCallback());

	if (metadata.isEditor()) {
				
		layout.formCallback(initContainerLayoutPropertyFormCallback()); 
		layout.setCallback(initContainerLayoutPropertySetCallback(command)); 

		dragger.moveWith(initContainerLayoutMoveCallback());
		dragger.dropWith(initContainmentDropCallback(command, containment, isRectangular));
		dragger.dropLocator(initContainerDropLocatorCallback(containment));
				
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
			initBasicBuildControls('Sizing', sizingEnumProperties, sizingEnumValues),
			containerSizingSelector
			);
			
		const traversal = initStyleContextMenuCallback(command, overlay, 
			traversalIcon,
			'Link Traversal Rules',
			initBasicBuildControls('Traversal', traversalEnumProperties, traversalEnumValues),
			containerSizingSelector,
			() => '');
		
		stylemenu.push(margins);
		stylemenu.push(padding);
		stylemenu.push(minSize);
		stylemenu.push(sizing);
		stylemenu.push(traversal);
		
		palette.addReveal(initLayoutIndicatorPaletteRevealCallback())
		initLayoutIndicator();
	}
	
}


once(() => initContainers());