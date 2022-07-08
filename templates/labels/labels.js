import { once } from '/github/kite9-org/kite9/client/bundles/ensure.js?v=v0.5'
import { initLabelContextMenuCallback } from '/github/kite9-org/kite9/behaviours/labels/label.js?v=v0.5' 
import { command, metadata, contextMenu } from '/github/kite9-org/kite9/templates/adl/adl.js?v=v0.5'

function initLabels() {
	
	if (metadata.isEditor()) {
		
		contextMenu.add(initLabelContextMenuCallback(command, document.params['label-template-uri']));
		
	}
}

once(() => initLabels());