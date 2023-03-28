import { hasLastSelected } from '../../../bundles/api.js';
import { getMainSvg } from '../../../bundles/screen.js';
export function initStyleMenuContextMenuCallback(submenu, selector = undefined) {
    if (selector == undefined) {
        selector = function () {
            return Array.from(getMainSvg().querySelectorAll("[id][k9-ui].selected"));
        };
    }
    return function (event, cm) {
        const selectedElements = hasLastSelected(selector());
        if (selectedElements.length > 0) {
            cm.addControl(event, "/public/behaviours/styleable/menu/style.svg", 'Styles', () => {
                cm.clear();
                submenu.forEach(item => {
                    item(event, cm);
                });
            });
        }
    };
}
