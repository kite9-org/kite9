import { getKite9Target, createUniqueId, changeId, getParentElement } from '../../../bundles/api.js';
import { getMainSvg, getSVGCoords, getElementPageBBox, currentTarget } from '../../../bundles/screen.js';
import { getElementUri } from '../../../classes/palette/palette.js';
import { setAutoconnectNew } from '../linkable.js';
function defaultDragableSelector(palettePanel) {
    return Array.from(palettePanel.querySelectorAll("[allow-drag=true] [id][k9-elem]"));
}
/** Internal state of palette drag */
let time = Date.now();
let position = { x: 0, y: 0 };
let mouseDown = false;
/**
 * Allows users to drag off the palette and link to items in the main document.
 */
export function initNewLinkPaletteLoadCallback(dragger, dragableSelector = undefined) {
    const DRAG_INTERVAL = 700; // 700ms for drag to start
    const DRAG_DIST = 3;
    if (dragableSelector == undefined) {
        dragableSelector = defaultDragableSelector;
    }
    return function (palette, palettePanel) {
        function getPaletteElement(event) {
            const choices = dragableSelector(palettePanel);
            let target = getKite9Target(currentTarget(event));
            while ((!choices.includes(target)) && (target != null)) {
                target = getParentElement(target);
            }
            return target;
        }
        function startDrag(event) {
            time = Date.now();
            position = getSVGCoords(event);
            mouseDown = true;
        }
        function endDrag() {
            mouseDown = false;
        }
        function isDragging(event) {
            if (!mouseDown) {
                return false;
            }
            if (Date.now() - time >= DRAG_INTERVAL) {
                return true;
            }
            const newPosition = getSVGCoords(event);
            const absChange = Math.abs(position.x - newPosition.x) + Math.abs(position.y - newPosition.y);
            if (absChange >= DRAG_DIST) {
                return true;
            }
            return false;
        }
        function moveDrag(event) {
            if (isDragging(event)) {
                mouseDown = false;
                const paletteElement = getPaletteElement(event);
                // create a new copy of the palette element
                const newId = createUniqueId();
                const droppingElement = paletteElement.cloneNode(true);
                changeId(droppingElement, droppingElement.getAttribute("id"), newId);
                setAutoconnectNew(droppingElement);
                // place it in the same position on the main svg 
                getMainSvg().appendChild(droppingElement);
                const mousePos = getSVGCoords(event);
                const boundBox = getElementPageBBox(droppingElement);
                const nx = mousePos.x - (boundBox.width / 2);
                const ny = mousePos.y - (boundBox.height / 2);
                const nt = "translate(" + nx + "," + ny + ")";
                droppingElement.setAttribute("transform", nt);
                droppingElement.classList.remove("selected");
                palette.destroy();
                const map = new Map();
                const uri = getElementUri(paletteElement, palettePanel);
                map.set(droppingElement, uri);
                dragger.beginAdd(map, event);
                dragger.grab();
                event.stopPropagation();
            }
        }
        dragableSelector(palettePanel).forEach(function (v) {
            v.removeEventListener("mousedown", startDrag);
            v.addEventListener("mousedown", startDrag);
            v.removeEventListener("touchstart", startDrag);
            v.addEventListener("touchstart", startDrag);
            v.removeEventListener("mousemove", moveDrag);
            v.addEventListener("mousemove", moveDrag);
            v.removeEventListener("touchmove", moveDrag);
            v.addEventListener("touchmove", moveDrag);
        });
        palettePanel.removeEventListener("mouseup", endDrag);
        palettePanel.addEventListener("mouseup", endDrag);
        palettePanel.removeEventListener("touchend", endDrag);
        palettePanel.addEventListener("touchend", endDrag);
    };
}
