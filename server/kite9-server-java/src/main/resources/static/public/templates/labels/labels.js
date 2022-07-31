import { once } from '/public/bundles/ensure.js'

import { command, placement, contextMenu, metadata } from '/public/templates/editor/editor.js'
import { initAddLabelContextMenuCallback } from '/public/behaviours/labels/add/labels-add.js' 
import { initPlaceLabelContextMenuCallback, initLabelPlacementPropertyFormCallback, initLabelPlacementPropertySetCallback } from '/public/behaviours/labels/place/labels-place.js' 

function initLabels() {
	
	if (metadata.isEditor()) {
	
		placement.formCallback(initLabelPlacementPropertyFormCallback(command)); 
		placement.setCallback(initLabelPlacementPropertySetCallback(command)); 
		
		contextMenu.add(initAddLabelContextMenuCallback(command, document.params['label-template-uri']));
		contextMenu.add(initPlaceLabelContextMenuCallback(placement, command));
		
	}
}

once(() => initLabels());