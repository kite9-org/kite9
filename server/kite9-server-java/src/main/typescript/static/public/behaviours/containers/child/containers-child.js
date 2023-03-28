import { hasLastSelected, createUniqueId } from '../../../bundles/api.js';
import { getMainSvg } from '../../../bundles/screen.js';
const defaultChildSelector = function () {
    return Array.from(getMainSvg().querySelectorAll("[k9-child].selected"));
};
/**
 * Adds child option into context menu
 */
export function initChildContextMenuCallback(command, selector = defaultChildSelector) {
    function getElementUri(e) {
        return e.getAttribute("k9-child");
    }
    function createInsertStep(e, uri) {
        return {
            "type": 'InsertUrl',
            "fragmentId": e.getAttribute('id'),
            "uriStr": uri,
            "deep": true,
            "newId": createUniqueId()
        };
    }
    /**
     * Provides a link option for the context menu
     */
    return function (event, contextMenu) {
        const selectedElements = hasLastSelected(selector());
        if (selectedElements.length > 0) {
            contextMenu.addControl(event, "/public/behaviours/containers/child/add.svg", "Add Child", function () {
                selectedElements.forEach(e => {
                    const uri = getElementUri(e);
                    if (uri != undefined) {
                        command.push(createInsertStep(e, uri));
                    }
                });
                command.perform();
                contextMenu.destroy();
            });
        }
    };
}
