/**
 * This composes the basic edit behaviour of the application
 */

import { metadata, transition, contextMenu } from '/github/kite9-org/kite9/templates/adl/adl.js';

// navigation
import { initFocusContextMenuCallback, initFocusMetadataCallback, initFocus } from "/github/kite9-org/kite9/behaviours/navigable/focus/focus.js";
import { initOpenContextMenuCallback } from "/github/kite9-org/kite9/behaviours/navigable/open/open.js";

// rest stuff
import { initNewDocumentContextMenuCallback, initTemplateSource } from "/github/kite9-org/kite9/behaviours/rest/NewDocument/NewDocument.js";

//selectable
import { initSelectable } from '/github/kite9-org/kite9/behaviours/selectable/selectable.js';

import { once } from '/github/kite9-org/kite9/bundles/ensure.js';


once(function() {
	metadata.add(initFocusMetadataCallback());
			
	contextMenu.add(initFocusContextMenuCallback(transition));
	contextMenu.add(initOpenContextMenuCallback(transition));
	contextMenu.add(initNewDocumentContextMenuCallback(transition, metadata, initTemplateSource()));
	
	initFocus(transition)
	
	initSelectable(undefined, true);
});
