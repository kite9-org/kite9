

import { once } from '/public/bundles/ensure.js'

import { Linker } from '/public/classes/linker/linker.js'


import { command, metadata, dragger, contextMenu, paletteContextMenu, containment, palette, overlay, stylemenu } from '/public/templates/editor/editor.js'

// Links
import { initLinkable, updateLink, initLinkerDropCallback } from '/public/behaviours/links/linkable.js'
import { initAutoConnectMoveCallback, initAutoConnectLinkerCallback, initAutoConnectTemplateSelector } from '/public/behaviours/links/autoconnect/autoconnect.js'
import { initLinkLinkerCallback, initLinkContextMenuCallback, getLinkTemplateUri } from '/public/behaviours/links/link/link.js'
import { initDirectionContextMenuCallback } from '/public/behaviours/links/direction/direction.js'
import { initAlignContextMenuCallback } from '/public/behaviours/links/align/align.js'
import { initTerminatorDropCallback, initTerminatorMoveCallback, initTerminatorDropLocatorFunction } from '/public/behaviours/links/drag/terminators-drag.js'
import { initLinkDropLocator, initLinkDropCallback } from '/public/behaviours/links/drag/links-drag.js'
import { initNewLinkPaletteCallback } from '/public/behaviours/links/new/links-new.js'
import { initLinksCheckerDropCallback } from '/public/behaviours/links/checker/links-checker.js'
import { initTerminatorContainmentCallback, initLabelContainmentCallback } from '/public/behaviours/links/rules/links-rules.js'
import { initSetDefaultContextMenuCallback } from '/public/behaviours/palettes/template/palettes-template.js';
import { initPaletteFinder } from '/public/behaviours/palettes/menu/palettes-menu.js';
import { initStyleContextMenuCallback } from '/public/behaviours/styleable/styleable.js';
import { initPortsPositionBuildControls, portsSelector, portsPositionIcon, initPortsPositionChangeEvent } from '/public/behaviours/ports/position/ports-position.js'

const linker = new Linker(updateLink);

export { linker }


function initLinks() {
	
	if (metadata.isEditor()) {
	
		const getAlignTemplateUri = () => document.params['align-template-uri'];
		const linkFinder = initPaletteFinder();
	
    	linker.add(initLinkLinkerCallback(command));
		linker.add(initAutoConnectLinkerCallback(command));
	
		dragger.dropLocatorFn(initTerminatorDropLocatorFunction());
		dragger.dropLocator(initLinkDropLocator());

		dragger.moveWith(initTerminatorMoveCallback());
	
		dragger.moveWith(initAutoConnectMoveCallback(linker, 
				linkFinder, 
				initAutoConnectTemplateSelector(getAlignTemplateUri, getLinkTemplateUri)));
		
		dragger.dropWith(initLinkDropCallback(command));
		dragger.dropWith(initTerminatorDropCallback(command));
		dragger.dropWith(initLinkerDropCallback(command, linker));
		dragger.dropWith(initLinksCheckerDropCallback(command));
		
		palette.add(initNewLinkPaletteCallback(dragger));

		contextMenu.add(initLinkContextMenuCallback(command, linker));
		contextMenu.add(initAlignContextMenuCallback(command));
		contextMenu.add(initDirectionContextMenuCallback(command));
		
		containment.add(initLabelContainmentCallback());
		containment.add(initTerminatorContainmentCallback());
		
		paletteContextMenu.add(initSetDefaultContextMenuCallback(palette, 'link-template-uri', "Link", linkFinder, p => p.querySelectorAll("[k9-elem=link]")));
		
		initLinkable(linker);
		
			
		const portPosition = initStyleContextMenuCallback(command, overlay, 
			portsPositionIcon,
			'Port Position',
			initPortsPositionBuildControls(),
			portsSelector,
			(r) => '', 
			initPortsPositionChangeEvent);
		
		stylemenu.push(portPosition);
	}
	
}

once(() => initLinks());
