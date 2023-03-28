import { hasLastSelected, parseInfo } from '../../../bundles/api.js';
import { getMainSvg } from '../../../bundles/screen.js';
import { intersects } from '../../../bundles/types.js';
export function initSelectContextMenuCallback(selector = undefined) {
    if (selector == undefined) {
        selector = function () {
            return Array.from(getMainSvg().querySelectorAll("[id][k9-ui~='grid'].selected"));
        };
    }
    function performSelect(_cm, _event, horiz, elements) {
        elements.forEach(e => {
            const info = parseInfo(e);
            const range = horiz ? info['grid-y'] : info['grid-x'];
            const container = e.parentElement;
            Array.from(container.children).forEach(f => {
                const details = parseInfo(f);
                if ((details != null) && details['grid-x']) {
                    const intersect = horiz ? intersects(details['grid-y'], range) :
                        intersects(details['grid-x'], range);
                    if (intersect) { //&& (!f.classList.contains('grid-temporary'))) {
                        f.classList.add("selected");
                    }
                }
            });
        });
    }
    /**
     * Provides overlays for selecting rows, columns
     */
    return function (event, cm) {
        const e = hasLastSelected(selector());
        if (e.length > 0) {
            cm.addControl(event, "/public/behaviours/grid/select/vertical.svg", "Select Column", () => performSelect(cm, event, false, selector()), 'Related');
            cm.addControl(event, "/public/behaviours/grid/select/horizontal.svg", "Select Row", () => performSelect(cm, event, true, selector()), 'Related');
        }
    };
}
