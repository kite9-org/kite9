/**
 * This composes the basic edit behaviour of the application
 */

import { metadata, transition, contextMenu } from '/github/kite9-org/kite9/templates/adl/adl.js?v=v0.7'

// navigation
import { initFocusContextMenuCallback, initFocusMetadataCallback, initFocus } from '/github/kite9-org/kite9/client/behaviours/navigable/focus/focus.js?v=v0.7'
import { initOpenContextMenuCallback } from '/github/kite9-org/kite9/client/behaviours/navigable/open/open.js?v=v0.7'

// rest stuff
import { initNewDocumentContextMenuCallback, initTemplateSource } from '/github/kite9-org/kite9/client/behaviours/rest/NewDocument/NewDocument.js?v=v0.7'

//selectable
import { initSelectable } from '/github/kite9-org/kite9/client/behaviours/selectable/selectable.js?v=v0.7'

import { once } from '/github/kite9-org/kite9/client/bundles/ensure.js?v=v0.7'


once(function() {
	metadata.add(initFocusMetadataCallback());
			
	contextMenu.add(initFocusContextMenuCallback(transition));
	contextMenu.add(initOpenContextMenuCallback(transition));
	contextMenu.add(initNewDocumentContextMenuCallback(transition, metadata, initTemplateSource()));
	
	initFocus(transition)
	
	initSelectable(undefined, true);
});
