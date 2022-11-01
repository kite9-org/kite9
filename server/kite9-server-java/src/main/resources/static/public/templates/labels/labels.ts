import { once } from '../../bundles/ensure.js'

import { command, placement, contextMenu, metadata, palette, paletteContextMenu } from '../../templates/editor/editor.js'
import { initAddLabelContextMenuCallback } from '../../behaviours/labels/add/labels-add.js' 
import { initPlaceLabelContextMenuCallback, initLabelPlacementPropertyFormCallback, initLabelPlacementPropertySetCallback } from '../../behaviours/labels/place/labels-place.js' 
import { initSetDefaultContextMenuCallback } from '../../behaviours/palettes/template/palettes-template.js';
import { initPaletteFinder } from '../../behaviours/palettes/menu/palettes-menu.js';
import { getDocumentParam } from '../../bundles/api.js';


function initLabels() {
	
	if (metadata.isEditor()) {
	
		placement.formCallback(initLabelPlacementPropertyFormCallback(command)); 
		placement.setCallback(initLabelPlacementPropertySetCallback(command)); 
		
		contextMenu.add(initAddLabelContextMenuCallback(command, getDocumentParam('label-template-uri')));
		contextMenu.add(initPlaceLabelContextMenuCallback(placement, command));
	
	
		paletteContextMenu.add(initSetDefaultContextMenuCallback(palette, 'label-template-uri', "Label", initPaletteFinder(), p => Array.from(p.querySelectorAll("[k9-elem=label]"))));
	
	}
}

once(() => initLabels());