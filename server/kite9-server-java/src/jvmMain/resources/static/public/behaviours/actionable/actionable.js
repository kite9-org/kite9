import { getMainSvg } from '../../bundles/screen.js';
import { addNamedEventListener } from '../../bundles/monika.js';
/**
 * Allows the context menu to appear when the user clicks an element with an id
 */
export function initActionable(contextMenu, selector = undefined) {
    /**
     * Displays a context menu when the user clicks on an element.
     */
    function click(event) {
        contextMenu.destroy();
        if (getMainSvg().style.cursor == 'wait') {
            return;
        }
        contextMenu.handle(event);
        event.stopPropagation();
    }
    if (selector == undefined) {
        selector = function () {
            return Array.from(getMainSvg().querySelectorAll("[id]"));
        };
    }
    window.addEventListener('DOMContentLoaded', function () {
        selector().forEach(function (v) {
            v.removeEventListener("click", click);
            addNamedEventListener(v, "click", "actionable", click);
        });
    });
}
