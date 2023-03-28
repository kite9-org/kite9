import { hasLastSelected, onlyLastSelected, isConnected, parseInfo } from '../../../bundles/api.js';
import { getSVGCoords, getElementPageBBox, getMainSvg } from '../../../bundles/screen.js';
import { drawBar, clearBar } from '../../../bundles/ordering.js';
import { svg } from '../../../bundles/screen.js';
function getLayout(e) {
    if (e == null) {
        return 'none';
    }
    else {
        const info = parseInfo(e);
        const l = info['layout'];
        return ((l == 'null') || (l == undefined)) ? undefined : l.toLowerCase();
    }
}
function getLayoutImage(layout) {
    return "/public/behaviours/containers/layout/" + layout + ".svg";
}
function drawLayout(event, cm, layout, cb, set = undefined, selected = undefined) {
    if (layout == undefined) {
        layout = "none";
    }
    const atts = { "style": "border-radius: 0px" };
    if (selected == layout) {
        atts['class'] = "selected";
    }
    cm.addControl(event, getLayoutImage(layout), layout, cb, set, atts);
}
const LAYOUTS = ["none", "right", "down", "horizontal", "vertical", "left", "up"];
export function initContainerLayoutPropertyFormCallback() {
    return function (propertyOwner, contextEvent, contextMenu, selectedElements) {
        const ls = onlyLastSelected(selectedElements);
        const layout = getLayout(ls);
        LAYOUTS.forEach(s => {
            drawLayout(contextEvent, contextMenu, s, (formEvent) => {
                propertyOwner.setProperty(contextEvent, formEvent, contextMenu, selectedElements);
            }, "Layout", layout);
        });
    };
}
export function initContainerLayoutPropertySetCallback(command) {
    return function (_propertyOwner, _contextEvent, formEvent, _contextMenu, selectedElements) {
        const layout = formEvent.currentTarget.getAttribute("aria-label");
        selectedElements.forEach(e => {
            const existing = getLayout(e);
            const id = e.getAttribute("id");
            if (LAYOUTS.includes(layout)) {
                command.push({
                    fragmentId: id,
                    type: 'ReplaceStyle',
                    name: '--kite9-layout',
                    to: layout == 'none' ? null : layout,
                    from: existing
                });
            }
        });
    };
}
export function initLayoutContextMenuCallback(layoutProperty, selector = undefined) {
    if (selector == undefined) {
        selector = function () {
            return Array.from(getMainSvg().querySelectorAll("[id][k9-ui~=layout].selected"));
        };
    }
    /**
     * Provides a layout option for the context menu
     */
    return function (event, contextMenu) {
        const e = hasLastSelected(selector());
        if (e.length > 0) {
            const ls = onlyLastSelected(e);
            const layout = getLayout(ls);
            drawLayout(event, contextMenu, layout, formEvent => {
                contextMenu.clear();
                layoutProperty.populateForm(formEvent, contextMenu, e);
            });
        }
    };
}
export function initContainerLayoutMoveCallback() {
    function updateBar(event, inside, horiz) {
        let fx, fy, tx, ty;
        const { x, y } = getSVGCoords(event);
        const contain = getElementPageBBox(inside);
        if (horiz) {
            fx = 0;
            tx = contain.width;
            fy = y - contain.y;
            ty = y - contain.y;
        }
        else {
            fx = x - contain.x;
            tx = x - contain.x;
            fy = 0;
            ty = contain.height;
        }
        drawBar(fx, fy, tx, ty, inside);
    }
    return function (dragTargets, event, dropTargets) {
        if (dragTargets.filter(dt => isConnected(dt)).length == 0) {
            // not dragging a connected, so we don't need a layout indicator
            return;
        }
        if (dropTargets) {
            const connectedDropTargets = dropTargets.filter(dt => isConnected(dt));
            if ((connectedDropTargets.length == 1)) {
                const dropInto = connectedDropTargets[0];
                const layout = getLayout(dropInto);
                if ((layout == 'up') || (layout == 'down') || (layout == "vertical")) {
                    // draw the horizontal bar
                    updateBar(event, dropInto, true);
                    return;
                }
                else if ((layout == 'left') || (layout == 'right') || (layout == 'horizontal')) {
                    updateBar(event, dropInto, false);
                    return;
                }
            }
        }
        clearBar();
    };
}
const INDICATOR_SELECTOR = ":scope > g.k9-layout";
function removeLayoutIndicator(e) {
    const indicator = e.querySelector(INDICATOR_SELECTOR);
    if (indicator) {
        e.removeChild(indicator);
    }
}
function ensureLayoutIndicator(e, layout) {
    let indicator = e.querySelector(INDICATOR_SELECTOR);
    if ((indicator != null) && (indicator.getAttribute("layout") != layout)) {
        e.removeChild(indicator);
    }
    else if (indicator != null) {
        return;
    }
    indicator = svg('g', {
        'class': 'k9-layout',
        'k9-highlight': 'stroke',
        'layout': layout,
    }, [
        svg('image', {
            x: 5,
            y: 5,
            width: 15,
            height: 15,
            href: getLayoutImage(layout)
        })
    ]);
    e.appendChild(indicator);
}
export function initLayoutIndicator(selector = undefined) {
    if (selector == undefined) {
        selector = function () {
            return Array.from(getMainSvg().querySelectorAll("[id][k9-ui~=layout]"));
        };
    }
    window.addEventListener('DOMContentLoaded', function () {
        selector().forEach(function (v) {
            const layout = getLayout(v);
            if (layout) {
                ensureLayoutIndicator(v, layout);
            }
            else {
                removeLayoutIndicator(v);
            }
        });
    });
}
export function initLayoutIndicatorPaletteRevealCallback(selector = undefined) {
    if (selector == undefined) {
        selector = function (e) {
            return Array.from(e.querySelectorAll("[id][k9-ui~=layout]"));
        };
    }
    return function (palettePanel) {
        if (palettePanel) {
            selector(palettePanel).forEach(function (v) {
                const layout = getLayout(v);
                if (layout) {
                    ensureLayoutIndicator(v, layout);
                }
                else {
                    removeLayoutIndicator(v);
                }
            });
        }
    };
}
