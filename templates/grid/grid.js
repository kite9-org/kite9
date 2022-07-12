
import { once } from '/github/kite9-org/kite9/client/bundles/ensure.js?v=v0.6'


//grid
import { initCellDragLocator, initCellDropCallback, initCellMoveCallback, initCellDropLocatorFunction } from '/github/kite9-org/kite9/client/behaviours/grid/drag/grid-drag.js?v=v0.6'
import { initGridTemporaryReplacePaletteCallback } from '/github/kite9-org/kite9/client/behaviours/grid/replace/grid-replace.js?v=v0.6'
import { initSelectContextMenuCallback } from '/github/kite9-org/kite9/client/behaviours/grid/select/grid-select.js?v=v0.6'
import { initCellAppendContextMenuCallback } from '/github/kite9-org/kite9/client/behaviours/grid/append/append.js?v=v0.6'
import { initGridContainmentCallback } from '/github/kite9-org/kite9/client/behaviours/grid/rules/grid-rules.js?v=v0.6'
import { initGridLayoutPropertyFormCallback, initGridLayoutPropertySetCallback } from '/github/kite9-org/kite9/client/behaviours/grid/layout/grid-layout.js?v=v0.6'
import { initCellCreator } from '/github/kite9-org/kite9/client/behaviours/grid/create/create.js?v=v0.6'


import { command, metadata, dragger, contextMenu, layout, palette, containment } from '/github/kite9-org/kite9/templates/editor/editor.js?v=v0.6'

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