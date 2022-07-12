

import { once } from '/github/kite9-org/kite9/client/bundles/ensure.js?v=v0.8'

import { Linker } from '/github/kite9-org/kite9/client/classes/linker/linker.js?v=v0.8'


import { command, metadata, instrumentation, dragger, contextMenu, containment, palette } from '/github/kite9-org/kite9/templates/editor/editor.js?v=v0.8'

// Links
import { initLinkable, updateLink, initLinkerDropCallback } from '/github/kite9-org/kite9/client/behaviours/links/linkable.js?v=v0.8'
import { initAutoConnectMoveCallback, initAutoConnectLinkerCallback, initAutoConnectTemplateSelector } from '/github/kite9-org/kite9/client/behaviours/links/autoconnect/autoconnect.js?v=v0.8'
import { initLinkPaletteCallback, initLinkLinkerCallback, initLinkContextMenuCallback, initLinkInstrumentationCallback, initLinkFinder, getLinkTemplateUri } from '/github/kite9-org/kite9/client/behaviours/links/link/link.js?v=v0.8'
import { initDirectionContextMenuCallback } from '/github/kite9-org/kite9/client/behaviours/links/direction/direction.js?v=v0.8'
import { initAlignContextMenuCallback } from '/github/kite9-org/kite9/client/behaviours/links/align/align.js?v=v0.8'
import { initTerminatorDropCallback, initTerminatorMoveCallback, initTerminatorDropLocatorFunction } from '/github/kite9-org/kite9/client/behaviours/links/drag/terminators-drag.js?v=v0.8'
import { initLinkDropLocator, initLinkDropCallback } from '/github/kite9-org/kite9/client/behaviours/links/drag/links-drag.js?v=v0.8'
import { initNewLinkPaletteCallback, initNewLinkContextMenuCallback } from '/github/kite9-org/kite9/client/behaviours/links/new/links-new.js?v=v0.8'
import { initLinksCheckerDropCallback } from '/github/kite9-org/kite9/client/behaviours/links/checker/links-checker.js?v=v0.8'

const linker = new Linker(updateLink);

export { linker }


function initLinks() {
	
	if (metadata.isEditor()) {
	
		
		instrumentation.add(initLinkInstrumentationCallback(palette));

		const getAlignTemplateUri = () => document.params['align-template-uri'];
	
    linker.add(initLinkLinkerCallback(command));
		linker.add(initAutoConnectLinkerCallback(command));
	
		dragger.dropLocatorFn(initTerminatorDropLocatorFunction());
		dragger.dropLocator(initLinkDropLocator());

		dragger.moveWith(initTerminatorMoveCallback());
	
		dragger.moveWith(initAutoConnectMoveCallback(linker, 
				initLinkFinder(), 
				initAutoConnectTemplateSelector(getAlignTemplateUri, getLinkTemplateUri)));
		
		dragger.dropWith(initLinkDropCallback(command));
		dragger.dropWith(initTerminatorDropCallback(command));
		dragger.dropWith(initLinkerDropCallback(command, linker));
		dragger.dropWith(initLinksCheckerDropCallback(command));
		
		palette.add(initLinkPaletteCallback());
		palette.add(initNewLinkPaletteCallback(dragger));

		contextMenu.add(initLinkContextMenuCallback(command, linker));
		contextMenu.add(initNewLinkContextMenuCallback(palette, containment));
		contextMenu.add(initAlignContextMenuCallback(command));
		contextMenu.add(initDirectionContextMenuCallback(command));
		
		initLinkable(linker);

	}
	
}

once(() => initLinks());
