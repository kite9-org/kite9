import { once } from '/public/bundles/ensure.js'
import { initLabelContextMenuCallback } from '/public/behaviours/labels/label.js' 
import { command, metadata, contextMenu } from '/public/templates/adl/adl.js'

function initLabels() {
	
	if (metadata.isEditor()) {
		
		contextMenu.add(initLabelContextMenuCallback(command, document.params['label-template-uri']));
		
	}
}

once(() => initLabels());