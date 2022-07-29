/**
 * This composes the basic edit behaviour of the application
 */

import { metadata, transition, contextMenu } from '/github/kite9-org/kite9/templates/adl/adl.js?v=v0.12'

// navigation
import { initFocusContextMenuCallback, initFocusMetadataCallback, initFocus } from '/public/behaviours/navigable/focus/focus.js'
import { initOpenContextMenuCallback } from '/public/behaviours/navigable/open/open.js'

// rest stuff
import { initNewDocumentContextMenuCallback, initTemplateSource } from '/public/behaviours/rest/NewDocument/NewDocument.js'

//selectable
import { initSelectable } from '/public/behaviours/selectable/selectable.js'

import { once } from '/public/bundles/ensure.js'


once(function() {
	metadata.add(initFocusMetadataCallback());
			
	contextMenu.add(initFocusContextMenuCallback(transition));
	contextMenu.add(initOpenContextMenuCallback(transition));
	contextMenu.add(initNewDocumentContextMenuCallback(transition, metadata, initTemplateSource()));
	
	initFocus(transition)
	
	initSelectable(undefined, true);
});
