import { getMainSvg } from '../../bundles/screen.js';
import { getExistingConnections, parseInfo } from '../../bundles/api.js';
import { reverseDirection } from '../../bundles/types.js';
import { addNamedEventListener } from '../../bundles/monika.js';
export const LINKER_MOVE = "linker-move";
export const LINKER_END = "linker-end";
/**
 * Contains the functionality for linking drawing links between selected elements
 * and a target.
 *
 * There are some basic expectations about the way links work embedded in this behaviour:
 *
 * 1.  <from> and <to> elements are present, and have reference="" to indicate where they link to.\
 * 2.  Labels are embedded within <from> and <to>.
 * 3.  Align links (links that the editor can remove) have the class "align".
 *
 * All of these are in any case defaults that can be overridden.
 */
export function initLinkable(linker, selector = undefined) {
    function move(event) {
        linker.move(event);
    }
    function end(event) {
        linker.end(event);
    }
    if (selector == undefined) {
        selector = function () {
            return Array.from(getMainSvg().querySelectorAll("[id][k9-elem]"));
        };
    }
    window.addEventListener('DOMContentLoaded', function () {
        selector().forEach(function (v) {
            addNamedEventListener(v, "mousemove", LINKER_MOVE, move);
            addNamedEventListener(v, "touchmove", LINKER_MOVE, move, { passive: false });
            addNamedEventListener(v, "mouseup", LINKER_END, end);
            addNamedEventListener(v, "touchend", LINKER_END, end);
        });
    });
}
export function initLinkerDropCallback(_command, linker) {
    return function (_dragState, event) {
        linker.end(event, false); // this false means that changes don't get performed immediately
    };
}
/**
 * This is for updating links when they are being dragged.
 */
export function updateLink(e, from, to) {
    e.querySelectorAll("[k9-animate=link]").forEach(f => {
        f.setAttribute("d", "M" + from.x + " " + from.y + "L" + to.x + " " + to.y);
    });
}
export function initAlignmentCollector(ai) {
    return function getAlignElementsAndDirections(id1, id2) {
        return getExistingConnections(id1, id2)
            .filter(ai)
            .map(e => {
            const parsed = parseInfo(e);
            const d = parsed['direction'];
            const ids = parsed['link'];
            const reversed = ids[0] == id2;
            return {
                element: e,
                direction: reversed ? reverseDirection(d) : d
            };
        });
    };
}
/**
 * Call this to tell autoconnect we're dealing with a new element
 */
export function setAutoconnectNew(e) {
    e.setAttribute("autoconnect", "new");
}
export function isAutoconnectNew(e) {
    return e.getAttribute("autoconnect") == 'new';
}
