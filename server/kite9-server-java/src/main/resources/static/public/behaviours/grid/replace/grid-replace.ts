import { getMainSvg } from '../../../bundles/screen.js'
import { createUniqueId, parseInfo, getKite9Target, isCell } from '../../../bundles/api.js'
import { getElementUri, PaletteLoadCallback } from '../../../classes/palette/palette.js';
import { Command } from '../../../classes/command/command.js';
import { PaletteSelector, Selector } from '../../../bundles/types.js';

/**
 * Allows you to select temporary grid elements.
 */
export const defaultReplaceCellSelector: Selector = () => {
	return Array.from(getMainSvg().querySelectorAll("[k9-info*='layout: GRID'] > .grid-temporary.selected"));
}

function initDefaultReplaceChoiceSelector(): PaletteSelector {
	return function(palettePanel) {
		return Array.from(palettePanel.querySelectorAll("[id][k9-elem]"))
			.filter(e => isCell(e));
	}
}

export function initGridTemporaryReplacePaletteCallback(
	command: Command,
	replaceChoiceSelector: PaletteSelector = undefined,
	replaceSelector: Selector = undefined): PaletteLoadCallback {

	if (replaceChoiceSelector == undefined) {
		replaceChoiceSelector = initDefaultReplaceChoiceSelector();
	}


	if (replaceSelector == undefined) {
		replaceSelector = defaultReplaceCellSelector;
	}

	function createReplaceStep(command, e, drop, palettePanel) {
		const uri = getElementUri(drop, palettePanel);

		if (e.classList.contains("grid-temporary")) {

			const newId = createUniqueId();
			const parent = getKite9Target(e.parentElement);
			const rectInfo = parseInfo(e);
			const position = rectInfo['position'];
			const newOccupies = position[0] + " " + position[1] + " " +
				position[2] + " " + position[3] + " ";

			command.push({
				"type": 'InsertUrl',
				"fragmentId": parent.getAttribute('id'),
				"uriStr": uri,
				"newId": newId,
			});

			command.push({
				"type": "ReplaceStyle",
				"fragmentId": newId,
				"name": "--kite9-occupies",
				"to": newOccupies
			});

			return true;
		} else {
			return false;
		}

	}

	return function(palette, palettePanel) {
		function click(event) {
			const selectedElements = replaceSelector();
			const droppingElement = palette.get().querySelector("[id].mouseover")
			const result = Array.from(selectedElements)
				.map(e => createReplaceStep(command, e, droppingElement, palettePanel))
				.reduce((a, b) => a || b, false);

			if (result) {
				palette.destroy();
				command.perform();
				event.stopPropagation();
			}
		}

		replaceChoiceSelector(palettePanel).forEach(function(v) {
			v.removeEventListener("click", click);
			v.addEventListener("click", click);
		})
	}
}

