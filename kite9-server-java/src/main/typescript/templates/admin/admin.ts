/**
 * This composes the basic edit behaviour of the application
 */
import { metadata, transition, contextMenu } from '../adl/adl.js'

// navigation
import { initFocus, initFocusContextMenuCallback, initFocusMetadataCallback } from '../../behaviours/navigable/focus/navigable-focus.js'
import { initOpenContextMenuCallback } from '../../behaviours/navigable/open/navigable-open.js'
import { initNewDocumentContextMenuCallback, initTemplateSource } from '../../behaviours/navigable/create/navigable-create.js'

//selectable
import { initSelectable } from '../../behaviours/selectable/selectable.js'

import { once } from '../../bundles/ensure.js'

once(function() {
	metadata.add(initFocusMetadataCallback());
			
	contextMenu.add(initFocusContextMenuCallback(transition, metadata));
	contextMenu.add(initOpenContextMenuCallback());
	contextMenu.add(initNewDocumentContextMenuCallback(metadata, initTemplateSource(metadata)));
	
	initFocus(transition, metadata);
	
	initSelectable(undefined, true);
});