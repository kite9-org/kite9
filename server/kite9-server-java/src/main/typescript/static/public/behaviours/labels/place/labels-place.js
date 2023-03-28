import { getMainSvg } from '../../../bundles/screen.js';
import { parseStyle } from '../../../bundles/css.js';
import { hasLastSelected, onlyLastSelected } from '../../../bundles/api.js';
import { directions } from '../../../bundles/types.js';
const placements = [...directions, null];
const STYLE_NAME = '--kite9-direction';
function getPlacement(command, e) {
    const adlElement = command.getADLDom(e.getAttribute("id"));
    const style = parseStyle(adlElement.getAttribute("style"));
    const l = style[STYLE_NAME];
    return l;
}
function drawPlacement(event, cm, placement, selected = 'ignore') {
    const icon = placement == null ? "none" : placement;
    const out = cm.addControl(event, "/public/behaviours/labels/place/" + icon + ".svg", "placement (" + placement + ")", undefined);
    const img = out.children[0];
    if (selected == placement) {
        img.setAttribute("class", "selected");
    }
    return img;
}
export function initPlaceLabelContextMenuCallback(placementProperty, command, selector = undefined) {
    if (selector == undefined) {
        selector = function () {
            return Array.from(getMainSvg().querySelectorAll("[id][k9-ui~=place].selected"));
        };
    }
    /**
     * Provides a placement option for the context menu
     */
    return function (event, contextMenu) {
        const e = hasLastSelected(selector());
        if (e.length > 0) {
            const ls = onlyLastSelected(e);
            const placement = getPlacement(command, ls);
            const img = drawPlacement(event, contextMenu, placement);
            img.addEventListener("click", formEvent => {
                contextMenu.clear();
                placementProperty.populateForm(formEvent, contextMenu, e);
            });
        }
    };
}
export function initLabelPlacementPropertyFormCallback(command) {
    return function (propertyOwner, contextEvent, contextMenu, selectedElements) {
        const ls = onlyLastSelected(selectedElements);
        const placement = getPlacement(command, ls);
        placements.forEach(s => {
            const img2 = drawPlacement(contextEvent, contextMenu, s, placement);
            if (placement != s) {
                if (s != null) {
                    img2.setAttribute("title", s);
                }
                img2.addEventListener("click", (formEvent) => propertyOwner.setProperty(contextEvent, formEvent, contextMenu, selectedElements));
            }
        });
    };
}
export function initLabelPlacementPropertySetCallback(command) {
    return function (_propertyOwner, _contextEvent, formEvent, _contextMenu, selectedElements) {
        const placement = formEvent.currentTarget.getAttribute("title");
        selectedElements.forEach(e => {
            const id = e.getAttribute("id");
            if (placements.includes(placement)) {
                command.push({
                    fragmentId: id,
                    type: 'ReplaceStyle',
                    name: STYLE_NAME,
                    to: placement,
                    from: getPlacement(command, e)
                });
            }
        });
    };
}
