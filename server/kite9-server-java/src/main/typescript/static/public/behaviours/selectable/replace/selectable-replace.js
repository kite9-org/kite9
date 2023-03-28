import { hasLastSelected, parseInfo, onlyLastSelected } from '../../../bundles/api.js';
import { getMainSvg } from '../../../bundles/screen.js';
import { getElementUri } from '../../../classes/palette/palette.js';
function initDefaultReplaceSelector() {
    return function () {
        return Array.from(getMainSvg().querySelectorAll("[id][k9-elem].selected"));
    };
}
function initDefaultReplaceChoiceSelector() {
    return function (palettePanel) {
        return Array.from(palettePanel.querySelectorAll("[id][k9-elem]"));
    };
}
export function initReplaceContextMenuCallback(palette, command, rules, containment, replaceChoiceSelector = undefined, replaceSelector = undefined, createReplaceStep = undefined, replaceChecker = undefined) {
    if (replaceChoiceSelector == undefined) {
        replaceChoiceSelector = initDefaultReplaceChoiceSelector();
    }
    if (replaceSelector == undefined) {
        replaceSelector = initDefaultReplaceSelector();
    }
    if (replaceChecker == undefined) {
        replaceChecker = function (oldElement, newElement) {
            return containment.canReplace(oldElement, newElement);
        };
    }
    if (createReplaceStep == undefined) {
        createReplaceStep = function (command, e, drop, palettePanel) {
            const uri = getElementUri(drop, palettePanel);
            const eId = e.getAttribute('id');
            const info = parseInfo(e);
            if (!info.temporary) {
                command.push({
                    "type": 'ReplaceTagUrl',
                    "fragmentId": eId,
                    "to": uri,
                    "from": command.getAdl(eId),
                    ...rules
                });
                return true;
            }
            else {
                return false;
            }
        };
    }
    /**
     * Provides a link option for the context menu
     */
    return function (event, contextMenu) {
        // this is the elements we are going to replace
        const selectedElements = hasLastSelected(replaceSelector());
        const lastSelectedElement = onlyLastSelected(replaceSelector());
        // this is the palette element we are going to replace it with
        const palettePanel = palette.getOpenPanel();
        const droppingElement = onlyLastSelected(replaceChoiceSelector(palettePanel));
        if (lastSelectedElement) {
            if (replaceChecker(lastSelectedElement, droppingElement)) {
                contextMenu.addControl(event, "/public/behaviours/selectable/replace/replace.svg", "Replace", function () {
                    contextMenu.destroy();
                    const result = Array.from(selectedElements)
                        .filter(e => replaceChecker(e, droppingElement))
                        .map(e => createReplaceStep(command, e, droppingElement, palettePanel))
                        .reduce((a, b) => a || b, false);
                    if (result) {
                        palette.destroy();
                        command.perform();
                        event.stopPropagation();
                    }
                });
            }
        }
    };
}
