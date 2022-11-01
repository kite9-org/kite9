

import { once } from '../../bundles/ensure.js'

import { Linker } from '../../classes/linker/linker.js'


import { command, metadata, dragger, contextMenu, paletteContextMenu, containment, palette, overlay, stylemenu } from '../../templates/editor/editor.js'

// Links
import { initLinkable, updateLink, initLinkerDropCallback } from '../../behaviours/links/linkable.js'
import { initAutoConnectMoveCallback, initAutoConnectLinkerCallback, initAutoConnectTemplateSelector } from '../../behaviours/links/autoconnect/links-autoconnect.js'
import { initLinkLinkerCallback, initLinkContextMenuCallback, getLinkTemplateUri } from '../../behaviours/links/link/links-link.js'
import { initDirectionContextMenuCallback, initTerminatorDirectionIndicator } from '../../behaviours/links/direction/links-direction.js'
import { initAlignContextMenuCallback } from '../../behaviours/links/align/links-align.js'
import { initTerminatorDropCallback, initTerminatorMoveCallback, initTerminatorDropLocatorFunction } from '../../behaviours/links/drag/terminators-drag.js'
import { initLinkDropLocator, initLinkDropCallback } from '../../behaviours/links/drag/links-drag.js'
import { initNewLinkPaletteLoadCallback } from '../../behaviours/links/new/links-new.js'
import { initLinksNavContextMenuCallback } from '../../behaviours/links/nav/links-nav.js'
import { initLinksCheckerDropCallback } from '../../behaviours/links/checker/links-checker.js'
import { initTerminatorContainmentCallback, initLabelContainmentCallback } from '../../behaviours/links/rules/links-rules.js'
import { initSetDefaultContextMenuCallback } from '../../behaviours/palettes/template/palettes-template.js';
import { initPaletteFinder } from '../../behaviours/palettes/menu/palettes-menu.js';
import { initStyleContextMenuCallback } from '../../behaviours/styleable/styleable.js';
import { initPortsPositionBuildControls, portsSelector, portsPositionIcon, initPortsPositionChangeEvent } from '../../behaviours/ports/position/ports-position.js'
import { initPortDropCallback, initPortMoveCallback } from '../../behaviours/ports/drag/ports-drag.js';
import { initPortsAddContextMenuCallback } from '../../behaviours/ports/add/ports-add.js';
import { singleSelect } from '../../behaviours/selectable/selectable.js';
import { getDocumentParam } from '../../bundles/api.js'

const linker = new Linker(updateLink);

export { linker }


function initLinks() {

	if (metadata.isEditor()) {

		const getAlignTemplateUri = () => getDocumentParam('align-template-uri');
		const paletteFinder = initPaletteFinder();

		linker.add(initLinkLinkerCallback(command));
		linker.add(initAutoConnectLinkerCallback(command));

		dragger.dropLocatorFn(initTerminatorDropLocatorFunction());
		dragger.dropLocator(initLinkDropLocator());
		dragger.dropWith(initPortDropCallback(command, containment));

		dragger.moveWith(initTerminatorMoveCallback());
		dragger.moveWith(initPortMoveCallback(containment));

		dragger.moveWith(initAutoConnectMoveCallback(linker,
			paletteFinder,
			initAutoConnectTemplateSelector(getAlignTemplateUri, getLinkTemplateUri)));

		dragger.dropWith(initLinkDropCallback(command));
		dragger.dropWith(initTerminatorDropCallback(command));
		dragger.dropWith(initLinkerDropCallback(command, linker));
		dragger.dropWith(initLinksCheckerDropCallback(command));

		palette.addLoad(initNewLinkPaletteLoadCallback(dragger));

		contextMenu.add(initLinkContextMenuCallback(linker));
		contextMenu.add(initAlignContextMenuCallback(command));
		contextMenu.add(initDirectionContextMenuCallback(command));
		contextMenu.add(initPortsAddContextMenuCallback(command, containment, paletteFinder));
		contextMenu.add(initLinksNavContextMenuCallback(singleSelect));

		containment.add(initLabelContainmentCallback());
		containment.add(initTerminatorContainmentCallback());

		paletteContextMenu.add(initSetDefaultContextMenuCallback(palette, 'link-template-uri', "Link", paletteFinder, p => Array.from(p.querySelectorAll("[k9-elem=link]"))));
		paletteContextMenu.add(initSetDefaultContextMenuCallback(palette, 'port-template-uri', "Port", paletteFinder, portsSelector));

		initLinkable(linker);
		initTerminatorDirectionIndicator();


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
