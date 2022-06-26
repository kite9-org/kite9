import { once } from '/github/kite9-org/kite9/bundles/ensure.js';
import { initLabelContextMenuCallback } from '/github/kite9-org/kite9/behaviours/labels/label.js'; 
import { command, metadata, contextMenu } from '/github/kite9-org/kite9/templates/adl/adl.js';

function initLabels() {
	
	if (metadata.isEditor()) {
		
		contextMenu.add(initLabelContextMenuCallback(command, document.params['label-template-uri']));
		
	}
}

once(() => initLabels());