

import { once } from '/public/bundles/ensure.js'

import { Linker } from '/public/classes/linker/linker.js'


import { command, metadata, dragger, contextMenu, containment, palette } from '/public/templates/editor/editor.js'

// Links
import { initLinkable, updateLink, initLinkerDropCallback, initLinkFinder } from '/public/behaviours/links/linkable.js'
import { initAutoConnectMoveCallback, initAutoConnectLinkerCallback, initAutoConnectTemplateSelector } from '/public/behaviours/links/autoconnect/autoconnect.js'
import { initLinkLinkerCallback, initLinkContextMenuCallback, getLinkTemplateUri } from '/public/behaviours/links/link/link.js'
import { initDirectionContextMenuCallback } from '/public/behaviours/links/direction/direction.js'
import { initAlignContextMenuCallback } from '/public/behaviours/links/align/align.js'
import { initTerminatorDropCallback, initTerminatorMoveCallback, initTerminatorDropLocatorFunction } from '/public/behaviours/links/drag/terminators-drag.js'
import { initLinkDropLocator, initLinkDropCallback } from '/public/behaviours/links/drag/links-drag.js'
import { initNewLinkPaletteCallback } from '/public/behaviours/links/new/links-new.js'
import { initLinksCheckerDropCallback } from '/public/behaviours/links/checker/links-checker.js'
import { initTerminatorContainmentCallback, initLabelContainmentCallback } from '/public/behaviours/links/rules/links-rules.js'
import { initPaletteUpdateDefaults } from '/public/behaviours/links/template/links-template.js';

const linker = new Linker(updateLink);

export { linker }


function initLinks() {
	
	if (metadata.isEditor()) {
	
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
		
		palette.add(initNewLinkPaletteCallback(dragger));
		palette.addUpdate(initPaletteUpdateDefaults(palette, initLinkFinder()));

		contextMenu.add(initLinkContextMenuCallback(command, linker));
		contextMenu.add(initAlignContextMenuCallback(command));
		contextMenu.add(initDirectionContextMenuCallback(command));
		
		containment.add(initLabelContainmentCallback());
		containment.add(initTerminatorContainmentCallback());
		
		initLinkable(linker);

	}
	
}

once(() => initLinks());
