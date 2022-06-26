
import { once } from '/github/kite9-org/kite9/bundles/ensure.js';


//grid
import { initCellDragLocator, initCellDropCallback, initCellMoveCallback, initCellDropLocatorFunction } from '/github/kite9-org/kite9/behaviours/grid/drag/grid-drag.js';
import { initGridTemporaryReplacePaletteCallback } from '/github/kite9-org/kite9/behaviours/grid/replace/grid-replace.js';
import { initSelectContextMenuCallback } from '/github/kite9-org/kite9/behaviours/grid/select/grid-select.js';
import { initCellAppendContextMenuCallback } from '/github/kite9-org/kite9/behaviours/grid/append/append.js';
import { initGridContainmentCallback } from '/github/kite9-org/kite9/behaviours/grid/rules/grid-rules.js';
import { initGridLayoutPropertyFormCallback, initGridLayoutPropertySetCallback } from '/github/kite9-org/kite9/behaviours/grid/layout/grid-layout.js';
import { initCellCreator } from '/github/kite9-org/kite9/behaviours/grid/create/create.js';


import { command, metadata, dragger, contextMenu, layout, palette, containment } from '/github/kite9-org/kite9/templates/editor/editor.js';

export function initGrid() {
	
	
	if (metadata.isEditor()) {
		
		layout.formCallback(initGridLayoutPropertyFormCallback());
		layout.setCallback(initGridLayoutPropertySetCallback(command, initCellCreator(command)));

		dragger.dropWith(initCellDropCallback(command));
		dragger.moveWith(initCellMoveCallback());

		dragger.dragLocator(initCellDragLocator());
		dragger.dropLocatorFn(initCellDropLocatorFunction())
		contextMenu.add(initCellAppendContextMenuCallback(command));
		
		palette.add(initGridTemporaryReplacePaletteCallback(command));
		contextMenu.add(initSelectContextMenuCallback());
  
    containment.add(initGridContainmentCallback());
	}	
}

once(() => initGrid());