/**
 * This composes the basic edit behaviour of the application
 */

import { metadata, transition, contextMenu } from '../adl/adl.js'

// navigation
import { initFocusContextMenuCallback, initFocusMetadataCallback, initFocus } from '../../behaviours/navigable/focus/focus.js'
import { initOpenContextMenuCallback } from '../../behaviours/navigable/open/open.js'

// rest stuff
import { initNewDocumentContextMenuCallback, initTemplateSource } from '../../behaviours/rest/NewDocument/NewDocument.js'

//selectable
import { initSelectable } from '../../behaviours/selectable/selectable.js'

import { once } from '../../bundles/ensure.js'

once(function() {
	metadata.add(initFocusMetadataCallback());
			
	contextMenu.add(initFocusContextMenuCallback(transition));
	contextMenu.add(initOpenContextMenuCallback(transition));
	contextMenu.add(initNewDocumentContextMenuCallback(metadata, initTemplateSource()));
	
	initFocus(transition);
	
	initSelectable(undefined, undefined, true);
});
