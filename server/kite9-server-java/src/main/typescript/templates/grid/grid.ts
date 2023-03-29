
import { once } from '../../bundles/ensure.js'


//grid
import { initCellDragLocator, initCellDropCallback, initCellMoveCallback, initCellDropLocatorCallback } from '../../behaviours/grid/drag/grid-drag.js'
import { initSelectContextMenuCallback } from '../../behaviours/grid/select/grid-select.js'
import { initCellAppendContextMenuCallback } from '../../behaviours/grid/append/grid-append.js'
import { initGridContainsCallback, initGridContainmentRuleCallback } from '../../behaviours/grid/rules/grid-rules.js'
import { initGridLayoutPropertyFormCallback, initGridLayoutPropertySetCallback } from '../../behaviours/grid/layout/grid-layout.js'
import { initCellCreator } from '../../behaviours/grid/create/grid-create.js'


import { command, metadata, dragger, contextMenu, layout, palette, containment, paletteContextMenu } from '../../templates/editor/editor.js'
import { initBiFilter } from '../../behaviours/typed/rules/typed-rules.js'
import { initContainmentDropCallback } from '../../behaviours/containers/drag/containers-drag.js'
import { initReplaceContextMenuCallback } from '../../behaviours/selectable/replace/selectable-replace.js'
import { gridTemporaryReplaceStep, initGridTemporaryReplaceChecker, initReplaceTemporaryCellChoiceSelector, replaceTemporaryCellSelector } from '../../behaviours/grid/replace/grid-replace.js'

export function initGrid() {


	if (metadata.isEditor()) {

		layout.formCallback(initGridLayoutPropertyFormCallback());
		layout.setCallback(initGridLayoutPropertySetCallback(command, initCellCreator(command)));

		dragger.dropWith(initContainmentDropCallback(command, 
			initBiFilter(containment, ['cell'],['table'])));
			
		dragger.dropWith(initContainmentDropCallback(command, 
			initBiFilter(containment, ['connected', 'label'],['cell'])));
			
		dragger.dropWith(initCellDropCallback(command));
		dragger.moveWith(initCellMoveCallback());

		dragger.dragLocator(initCellDragLocator());
		dragger.dropLocator(initCellDropLocatorCallback())
		contextMenu.add(initCellAppendContextMenuCallback(command));

		paletteContextMenu.add(initReplaceContextMenuCallback(palette, command, {keptAttributes: ['id', 'reference', 'end', 'style']}, containment,
			initReplaceTemporaryCellChoiceSelector(),
			replaceTemporaryCellSelector, 
			gridTemporaryReplaceStep,
			initGridTemporaryReplaceChecker()));
		contextMenu.add(initSelectContextMenuCallback());

		containment.addContainmentRuleCallback(initGridContainmentRuleCallback());
		containment.addContainsCallback(initGridContainsCallback());
	}
}

once(() => initGrid());