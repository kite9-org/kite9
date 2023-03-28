import { getMainSvg } from '../../../bundles/screen.js';
import { createUniqueId, parseInfo, getKite9Target, isTemporary } from '../../../bundles/api.js';
import { getElementUri } from '../../../classes/palette/palette.js';
import { isCell } from '../../../behaviours/grid/common-grid.js';
/**
 * Allows you to select temporary grid elements.
 */
export const replaceTemporaryCellSelector = () => {
    return Array.from(getMainSvg().querySelectorAll("[id][k9-elem].selected"))
        .filter(e => isCell(e) && isTemporary(e));
};
/**
 * Items that could be selected from the palette to replace a temporary element
 */
export function initReplaceTemporaryCellChoiceSelector() {
    return function (palettePanel) {
        return Array.from(palettePanel.querySelectorAll("[id][k9-elem]"))
            .filter(e => isCell(e));
    };
}
export const gridTemporaryReplaceStep = function (command, e, drop, palettePanel) {
    const uri = getElementUri(drop, palettePanel);
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
};
export function initGridTemporaryReplaceChecker() {
    return function (_e1, _e2) {
        return true;
    };
}
