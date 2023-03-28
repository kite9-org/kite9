import { getMainSvg } from '../../../bundles/screen.js';
import { onlyLastSelected } from '../../../bundles/api.js';
export function initOpenContextMenuCallback(selector = undefined) {
    if (selector == undefined) {
        selector = function () {
            return Array.from(getMainSvg().querySelectorAll("[id][k9-ui~=open]"));
        };
    }
    /**
     * Provides a link option for the context menu
     */
    return function (event, contextMenu) {
        const e = onlyLastSelected(selector());
        if (e) {
            contextMenu.addControl(event, "/public/behaviours/navigable/open/open.svg", "Open", function () {
                contextMenu.destroy();
                const url = e.getAttribute("id");
                window.location.href = url;
            });
        }
    };
}
