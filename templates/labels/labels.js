import { once } from '/public/bundles/ensure.js'
import { initLabelContextMenuCallback } from '/public/behaviours/labels/label.js' 
import { command, metadata, contextMenu } from '/github/kite9-org/kite9/templates/adl/adl.js?v=v0.9'

function initLabels() {
	
	if (metadata.isEditor()) {
		
		contextMenu.add(initLabelContextMenuCallback(command, document.params['label-template-uri']));
		
	}
}

once(() => initLabels());