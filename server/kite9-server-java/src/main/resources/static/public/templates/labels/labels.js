import { once } from '/public/bundles/ensure.js'

import { command, placement, contextMenu, metadata, palette, paletteContextMenu } from '/public/templates/editor/editor.js'
import { initAddLabelContextMenuCallback } from '/public/behaviours/labels/add/labels-add.js' 
import { initPlaceLabelContextMenuCallback, initLabelPlacementPropertyFormCallback, initLabelPlacementPropertySetCallback } from '/public/behaviours/labels/place/labels-place.js' 
import { initSetDefaultContextMenuCallback } from '/public/behaviours/palettes/template/palettes-template.js';
import { initPaletteFinder } from '/public/behaviours/palettes/menu/palettes-menu.js';


function initLabels() {
	
	if (metadata.isEditor()) {
	
		placement.formCallback(initLabelPlacementPropertyFormCallback(command)); 
		placement.setCallback(initLabelPlacementPropertySetCallback(command)); 
		
		contextMenu.add(initAddLabelContextMenuCallback(command, document.params['label-template-uri']));
		contextMenu.add(initPlaceLabelContextMenuCallback(placement, command));
	
	
		paletteContextMenu.add(initSetDefaultContextMenuCallback(palette, 'label-template-uri', "Label", initPaletteFinder(), p => p.querySelectorAll("[k9-elem=label]")));
	
	}
}

once(() => initLabels());