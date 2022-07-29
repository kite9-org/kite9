import { once } from '/public/bundles/ensure.js'
import { initAddLabelContextMenuCallback } from '/public/behaviours/labels/add/labels-add.js' 
import { initPlaceLabelContextMenuCallback } from '/public/behaviours/labels/place/labels-place.js' 
import { command, metadata, contextMenu } from '/public/templates/adl/adl.js'

function initLabels() {
	
	if (metadata.isEditor()) {
		
		contextMenu.add(initAddLabelContextMenuCallback(command, document.params['label-template-uri']));
		
	}
}

once(() => initLabels());