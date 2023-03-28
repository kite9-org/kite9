import { getSVGCoords, getElementPageBBox, closestSide } from '../../../bundles/screen.js';
import { onlyUnique, isPort, getAffordances } from '../../../bundles/api.js';
import { parseStyle } from '../../../bundles/css.js';
import { drawBar, clearBar } from '../../../bundles/ordering.js';
/**
 * Decorates the drop callback to ensure the _side_ of the container is
 * set when dropping.
 */
export function initPortDropCallback(command, filter) {
    return function (dragState, evt, dropTargets) {
        dropTargets.forEach(dropTarget => {
            const side = closestSide(dropTarget, getSVGCoords(evt));
            dragState.forEach(s => {
                if (filter(s.dragTarget, dropTarget)) {
                    const canReposition = getAffordances(s.dragTarget).includes("direction");
                    const style = parseStyle(s.dragTarget.getAttribute("style"));
                    if (canReposition) {
                        console.log("Invoking port drop callback");
                        command.push({
                            fragmentId: s.dragTarget.getAttribute("id"),
                            type: 'ReplaceStyle',
                            name: '--kite9-direction',
                            to: side,
                            from: style['--kite9-direction']
                        });
                    }
                }
            });
        });
    };
}
export function initPortMoveCallback(containment) {
    function updateBar(inside, side) {
        const shape = inside.querySelector('.k9-shape');
        const { width, height } = getElementPageBBox(shape);
        switch (side) {
            case 'up':
                drawBar(0, 0, width, 0, inside);
                return;
            case 'right':
                drawBar(width, 0, width, height, inside);
                return;
            case 'down':
                drawBar(0, height, width, height, inside);
                return;
            case 'left':
                drawBar(0, 0, 0, height, inside);
                return;
        }
    }
    return function (dragTargets, event, dropTargets) {
        if (dragTargets.filter(dt => isPort(dt)).length == 0) {
            // not dragging a port
            return;
        }
        if (dropTargets) {
            const connectedDropTargets = dropTargets.filter(t => {
                return containment.canContainAll(dragTargets, t);
            }).filter(onlyUnique);
            if ((connectedDropTargets.length > 0)) {
                const dropInto = connectedDropTargets[0];
                const side = closestSide(dropInto, getSVGCoords(event));
                updateBar(dropInto, side);
                return;
            }
        }
        clearBar();
    };
}
