import { getMainSvg, currentTarget } from '../../bundles/screen.js';
import { getKite9Target } from '../../bundles/api.js';
export class Hover {
    constructor() {
        this.selectedElement = null;
    }
    hover(v) {
        if (v != this.selectedElement) {
            v.classList.add("mouseover");
            if (this.selectedElement != undefined) {
                this.selectedElement.classList.remove("mouseover");
            }
            this.selectedElement = v;
        }
    }
    unhover(v) {
        v.classList.remove("mouseover");
        if (this.selectedElement == v) {
            this.selectedElement = undefined;
        }
    }
    isHover(v) {
        return v.classList.contains("mouseover");
    }
}
/**
 * Adds hoverable behaviour
 */
export function initHoverable(selector = undefined, allowed = undefined, ctx = new Hover()) {
    if (allowed == undefined) {
        allowed = function (v) {
            return v != undefined;
        };
    }
    function mouseover(event) {
        if (event.handledHover) {
            return;
        }
        const v = getKite9Target(currentTarget(event));
        if ((v != null) && (allowed(v))) {
            ctx.hover(v);
            // prevents parent elements from highlighting too.
            event.handledHover = true;
        }
    }
    function mouseout(event) {
        const v = getKite9Target(currentTarget(event));
        if (allowed(v)) {
            ctx.unhover(v);
        }
    }
    if (selector == undefined) {
        selector = function () {
            return Array.from(getMainSvg().querySelectorAll("[id][k9-elem]"));
        };
    }
    window.addEventListener('DOMContentLoaded', function () {
        selector().forEach(function (v) {
            v.removeEventListener("mouseover", mouseover);
            v.addEventListener("mouseover", mouseover);
            v.removeEventListener("mouseout", mouseout);
            v.addEventListener("mouseout", mouseout);
            v.removeEventListener("touchmove", mouseover);
            v.addEventListener("touchmove", mouseover, { passive: false });
        });
    });
}
