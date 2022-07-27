

import { once } from '/public/bundles/ensure.js'

import { Linker } from '/public/classes/linker/linker.js'


import { command, metadata, instrumentation, dragger, contextMenu, containment, palette } from '/github/kite9-org/kite9/templates/editor/editor.js?v=v0.11'

// Links
import { initLinkable, updateLink, initLinkerDropCallback } from '/public/behaviours/links/linkable.js'
import { initAutoConnectMoveCallback, initAutoConnectLinkerCallback, initAutoConnectTemplateSelector } from '/public/behaviours/links/autoconnect/autoconnect.js'
import { initLinkPaletteCallback, initLinkLinkerCallback, initLinkContextMenuCallback, initLinkInstrumentationCallback, initLinkFinder, getLinkTemplateUri } from '/public/behaviours/links/link/link.js'
import { initDirectionContextMenuCallback } from '/public/behaviours/links/direction/direction.js'
import { initAlignContextMenuCallback } from '/public/behaviours/links/align/align.js'
import { initTerminatorDropCallback, initTerminatorMoveCallback, initTerminatorDropLocatorFunction } from '/public/behaviours/links/drag/terminators-drag.js'
import { initLinkDropLocator, initLinkDropCallback } from '/public/behaviours/links/drag/links-drag.js'
import { initNewLinkPaletteCallback, initNewLinkContextMenuCallback } from '/public/behaviours/links/new/links-new.js'
import { initLinksCheckerDropCallback } from '/public/behaviours/links/checker/links-checker.js'

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
