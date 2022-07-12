import { once } from '/github/kite9-org/kite9/client/bundles/ensure.js?v=v0.8'
import { initLabelContextMenuCallback } from '/github/kite9-org/kite9/client/behaviours/labels/label.js?v=v0.8' 
import { command, metadata, contextMenu } from '/github/kite9-org/kite9/templates/adl/adl.js?v=v0.8'

function initLabels() {
	
	if (metadata.isEditor()) {
		
		contextMenu.add(initLabelContextMenuCallback(command, document.params['label-template-uri']));
		
	}
}

once(() => initLabels());