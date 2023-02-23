

import { once } from '../../bundles/ensure.js'

import { Linker } from '../../classes/linker/linker.js'


import { command, metadata, dragger, contextMenu, paletteContextMenu, containment, palette, overlay, stylemenu } from '../../templates/editor/editor.js'

// Links
import { initLinkable, updateLink, initLinkerDropCallback, initAlignmentCollector, AlignmentIdentifier } from '../../behaviours/links/linkable.js'
import { initAutoConnectMoveCallback, initAutoConnectLinkerCallback, initAutoConnectTemplateSelector } from '../../behaviours/links/autoconnect/links-autoconnect.js'
import { initLinkLinkerCallback, initLinkContextMenuCallback, getLinkTemplateUri } from '../../behaviours/links/link/links-link.js'
import { initLinkDirectionContextMenuCallback, initTerminatorDirectionIndicator } from '../../behaviours/links/direction/links-direction.js'
import { initAlignContextMenuCallback } from '../../behaviours/links/align/links-align.js'
import { initTerminatorDropCallback, initTerminatorMoveCallback } from '../../behaviours/links/drag/terminators-drag.js'
import { initLinkDropLocator, initLinkDropCallback } from '../../behaviours/links/drag/links-drag.js'
import { initNewLinkPaletteLoadCallback } from '../../behaviours/links/new/links-new.js'
import { initLinksNavContextMenuCallback } from '../../behaviours/links/nav/links-nav.js'
import { initLinksCheckerDropCallback } from '../../behaviours/links/checker/links-checker.js'
import { initSetDefaultContextMenuCallback } from '../../behaviours/palettes/template/palettes-template.js';
import { initPaletteFinder } from '../../behaviours/palettes/menu/palettes-menu.js';
import { initStyleContextMenuCallback } from '../../behaviours/styleable/styleable.js';
import { initPortsPositionBuildControls, initPortsSelector, portsPositionIcon, initPortsPositionChangeEvent } from '../../behaviours/ports/position/ports-position.js'
import { initPortDropCallback, initPortMoveCallback } from '../../behaviours/ports/drag/ports-drag.js';
import { initPortsAddContextMenuCallback } from '../../behaviours/ports/add/ports-add.js';
import { singleSelect } from '../../behaviours/selectable/selectable.js';
import { getDocumentParam } from '../../bundles/api.js'
import { initBiFilter } from '../../behaviours/typed/rules/typed-rules.js'
import { initContainmentDropCallback } from '../../behaviours/containers/drag/containers-drag.js'

const linker = new Linker(updateLink);

export { linker }


function initLinks() {

	if (metadata.isEditor()) {

		const getAlignTemplateUri = () => getDocumentParam('align-template-uri');
		const paletteFinder = initPaletteFinder();
		const alignmentIdentifier : AlignmentIdentifier = (e) => e.getAttribute("k9-elem") == "align";
		const alignmentCollector = initAlignmentCollector(alignmentIdentifier);

		linker.add(initLinkLinkerCallback(command, alignmentCollector));
		linker.add(initAutoConnectLinkerCallback(command, alignmentIdentifier));

		dragger.dropLocator(initLinkDropLocator());
			
		dragger.dropWith(initContainmentDropCallback(command, initBiFilter(containment, ['port'],['connected'])));	
		dragger.dropWith(initPortDropCallback(command, initBiFilter(containment, ['port'],['*'])));

		dragger.moveWith(initTerminatorMoveCallback());
		dragger.moveWith(initPortMoveCallback(containment));

		dragger.moveWith(initAutoConnectMoveCallback(linker,
			paletteFinder,
			initAutoConnectTemplateSelector(getAlignTemplateUri, getLinkTemplateUri)));

		dragger.dropWith(initLinkDropCallback(command));
		dragger.dropWith(initTerminatorDropCallback(command, initBiFilter(containment, ['terminator'],['*'])));
		dragger.dropWith(initLinkerDropCallback(command, linker));
		dragger.dropWith(initLinksCheckerDropCallback(command));

		palette.addLoad(initNewLinkPaletteLoadCallback(dragger));

		contextMenu.add(initLinkContextMenuCallback(linker));
		contextMenu.add(initAlignContextMenuCallback(command, alignmentIdentifier));
		contextMenu.add(initLinkDirectionContextMenuCallback(command));
		contextMenu.add(initPortsAddContextMenuCallback(command, containment, paletteFinder));
		contextMenu.add(initLinksNavContextMenuCallback(singleSelect));

		paletteContextMenu.add(initSetDefaultContextMenuCallback(palette, 'link-template-uri', "Link", paletteFinder, p => Array.from(p.querySelectorAll("[k9-elem=link]"))));
		paletteContextMenu.add(initSetDefaultContextMenuCallback(palette, 'port-template-uri', "Port", paletteFinder, initPortsSelector()));

		initLinkable(linker);
		initTerminatorDirectionIndicator();


		const portPosition = initStyleContextMenuCallback(command, overlay,
			portsPositionIcon,
			'Port Position',
			initPortsPositionBuildControls(),
			initPortsSelector(),
			() => '',
			initPortsPositionChangeEvent);

		stylemenu.push(portPosition);
	}

}

once(() => initLinks());
