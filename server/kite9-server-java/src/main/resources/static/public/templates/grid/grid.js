
import { once } from '/public/bundles/ensure.js'


//grid
import { initCellDragLocator, initCellDropCallback, initCellMoveCallback, initCellDropLocatorFunction } from '/public/behaviours/grid/drag/grid-drag.js'
import { initGridTemporaryReplacePaletteCallback } from '/public/behaviours/grid/replace/grid-replace.js'
import { initSelectContextMenuCallback } from '/public/behaviours/grid/select/grid-select.js'
import { initCellAppendContextMenuCallback } from '/public/behaviours/grid/append/append.js'
import { initGridContainmentCallback } from '/public/behaviours/grid/rules/grid-rules.js'
import { initGridLayoutPropertyFormCallback, initGridLayoutPropertySetCallback } from '/public/behaviours/grid/layout/grid-layout.js'
import { initCellCreator } from '/public/behaviours/grid/create/create.js'


import { command, metadata, dragger, contextMenu, layout, palette, containment } from '/github/kite9-org/kite9/templates/editor/editor.js?v=v0.13'

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