
import { once } from '../../bundles/ensure.js'


//grid
import { initCellDragLocator, initCellDropCallback, initCellMoveCallback, initCellDropLocatorFunction } from '../../behaviours/grid/drag/grid-drag.js'
import { initGridTemporaryReplacePaletteCallback } from '../../behaviours/grid/replace/grid-replace.js'
import { initSelectContextMenuCallback } from '../../behaviours/grid/select/grid-select.js'
import { initCellAppendContextMenuCallback } from '../../behaviours/grid/append/append.js'
import { initGridContainmentCallback } from '../../behaviours/grid/rules/grid-rules.js'
import { initGridLayoutPropertyFormCallback, initGridLayoutPropertySetCallback } from '../../behaviours/grid/layout/grid-layout.js'
import { initCellCreator } from '../../behaviours/grid/create/create.js'


import { command, metadata, dragger, contextMenu, layout, palette, containment } from '../../templates/editor/editor.js'

export function initGrid() {


	if (metadata.isEditor()) {

		layout.formCallback(initGridLayoutPropertyFormCallback());
		layout.setCallback(initGridLayoutPropertySetCallback(command, initCellCreator(command)));

		//dragger.dropWith(initContainmentDropCallback(..., initCellDropCallback(command)));
		dragger.moveWith(initCellMoveCallback());

		dragger.dragLocator(initCellDragLocator());
		dragger.dropLocatorFn(initCellDropLocatorFunction())
		contextMenu.add(initCellAppendContextMenuCallback(command));

		palette.addLoad(initGridTemporaryReplacePaletteCallback(command));
		contextMenu.add(initSelectContextMenuCallback());

		containment.add(initGridContainmentCallback());
	}
}

once(() => initGrid());